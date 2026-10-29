import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { TuiButton, TuiDropdown, TuiFilterByInputPipe, TuiIcon, tuiItemsHandlersProvider } from '@taiga-ui/core';
import {
  TuiAvatar,
  TuiBadge,
  TuiButtonLoading,
  TuiChevron,
  TuiComboBox,
  TuiDataListWrapper,
  TuiSelect,
} from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AsistenciaService } from '../asistencia-service';
import { ActividadService } from '../reporte-qr-actividad-service';
import { AgendaService, InscripcionService } from '../../actividades/agenda-service';
import type { Actividad as ActividadAgenda } from '../../actividades/models';
import { PersonaService } from '../../personal/persona/persona-service';
import type { Persona } from '../../personal/persona/persona.model';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { GeolocalizacionService } from '../../../../core/services/geolocalizacion-service';
import { Asistencia, Actividad, CierreAsistencias } from '../models';

export interface FilaAsistencia {
  idPersona: number;
  nombre: string;
  asistencia: Asistencia | null;
}

@Component({
  selector: 'app-asistencia-list',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    TuiAvatar,
    TuiButton,
    TuiButtonLoading,
    TuiBadge,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown, TuiFilterByInputPipe,
    TuiIcon,
    TuiSelect,
    TuiTable,
  ],
  templateUrl: './asistencia-list.html',
  styleUrl: './asistencia-list.css',
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal(
        (item: unknown): string => {
          const o = item as { nombre?: string; idActividad?: unknown } | null;
          return o?.nombre ?? (o?.idActividad !== undefined && o?.idActividad !== null ? `Actividad #${String(o.idActividad)}` : '');
        }
      ),
      identityMatcher: signal(
        (a: unknown, b: unknown): boolean =>
          ((a as { idActividad?: unknown } | null)?.idActividad ?? null) ===
          ((b as { idActividad?: unknown } | null)?.idActividad ?? null)
      ),
    }),
  ],
})
export class AsistenciaList implements OnInit {
  private readonly asistencias = inject(AsistenciaService);
  private readonly actividadesSrv = inject(ActividadService);
  private readonly agenda = inject(AgendaService);
  private readonly inscripcionesSrv = inject(InscripcionService);
  private readonly personasSrv = inject(PersonaService);
  private readonly confirmar = inject(ConfirmService);
  protected readonly geo = inject(GeolocalizacionService);

  protected readonly actividades = signal<Actividad[]>([]);
  protected readonly items = signal<Asistencia[]>([]);
  protected readonly inscritos = signal<number[]>([]);
  protected readonly personas = signal<Persona[]>([]);
  protected readonly actividadDetalle = signal<ActividadAgenda | null>(null);
  protected readonly cierre = signal<CierreAsistencias | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly cerrando = signal(false);
  protected readonly registrando = signal<number[]>([]);
  protected readonly registrandoTodos = signal(false);

  /** Cabecera: esperados = solo inscritos explícitos (igual que el cierre backend). */
  protected readonly resumen = computed(() => {
    const regs = this.items();
    const esperadosIds = this.esperadosIds();
    const conMarca = new Set(regs.map((r) => r.idPersona));
    const presentes = regs.filter((r) => r.estado === 'ASISTIO').length;
    const tardes = regs.filter((r) => r.estado === 'TARDE').length;
    const faltas = regs.filter((r) => r.estado === 'AUSENTE').length;
    const exonerados = regs.filter((r) => r.estado === 'EXONERADO').length;
    const asistentes = presentes + tardes;
    const pendientes = Math.max(0, esperadosIds.length - conMarca.size);
    const porcentaje = esperadosIds.length > 0
      ? Math.round((asistentes * 100) / esperadosIds.length)
      : 0;
    return {
      esperados: esperadosIds.length,
      asistentes,
      presentes,
      tardes,
      faltas,
      exonerados,
      pendientes,
      porcentaje,
    };
  });

  /** Detalle: una fila por cada esperado + registrados fuera de lista. */
  protected readonly filas = computed<FilaAsistencia[]>(() => {
    const porPersona = new Map<number, Asistencia>();
    for (const a of this.items()) {
      if (!porPersona.has(a.idPersona)) porPersona.set(a.idPersona, a);
    }
    const nombres = new Map<number, string>();
    for (const p of this.personas()) {
      nombres.set(p.id, [p.apellidoPaterno, p.apellidoMaterno, p.nombres].filter(Boolean).join(' '));
    }
    const filas: FilaAsistencia[] = this.esperadosIds().map((id) => ({
      idPersona: id,
      nombre: nombres.get(id) ?? `Persona #${id}`,
      asistencia: porPersona.get(id) ?? null,
    }));
    for (const [id, a] of porPersona) {
      if (!this.esperadosIds().includes(id)) {
        filas.push({ idPersona: id, nombre: nombres.get(id) ?? `Persona #${id}`, asistencia: a });
      }
    }
    return filas.sort((x, y) => Number(x.asistencia == null) - Number(y.asistencia == null) || x.nombre.localeCompare(y.nombre));
  });

  private esperadosIds(): number[] {
    return [...new Set(this.inscritos())];
  }

  protected readonly actividadCtrl = new FormControl<Actividad | null>(null);

  protected readonly matcherActividad = (
    a: Actividad | null | undefined,
    b: Actividad | null | undefined
  ): boolean => (a?.idActividad ?? null) === (b?.idActividad ?? null);

  ngOnInit(): void {
    this.actividadesSrv.listar().pipe(catchError(() => of([]))).subscribe((acts) => {
      this.actividades.set(acts);
      if (acts.length > 0) {
        this.actividadCtrl.setValue(acts[0], { emitEvent: false });
        this.cargar();
      }
    });
    this.actividadCtrl.valueChanges.subscribe(() => {
      this.cierre.set(null);
      this.cargar();
    });
    // GPS obligatorio: captura automática al entrar (sin ingreso manual).
    this.geo.detectar().subscribe({ next: () => undefined, error: () => undefined });
  }

  detectarUbicacion(): void {
    this.error.set(null);
    this.geo.detectar().subscribe({ next: () => undefined, error: () => this.error.set(this.geo.error()) });
  }

  protected gpsListo(): boolean {
    return this.geo.estado() === 'ok' && this.geo.ubicacion() != null;
  }

  protected enlaceMiUbicacion(): string | null {
    const u = this.geo.ubicacion();
    return u ? GeolocalizacionService.enlaceGoogleMaps(u.latitud, u.longitud) : null;
  }

  protected async compartirMiUbicacion(): Promise<void> {
    const u = this.geo.ubicacion();
    if (!u) return;
    const texto = GeolocalizacionService.textoCompartir('Mi ubicación', u.latitud, u.longitud);
    try {
      if (typeof navigator.share === 'function') {
        await navigator.share({ title: 'Mi ubicación', text: texto });
        return;
      }
      throw new Error('sin share');
    } catch {
      try {
        await navigator.clipboard.writeText(texto);
      } catch {
        window.prompt('Copia tu ubicación:', texto);
      }
    }
  }

  cargar(): void {
    const act = this.actividadCtrl.value;
    if (!act?.idActividad) {
      this.items.set([]);
      this.inscritos.set([]);
      this.actividadDetalle.set(null);
      return;
    }
    const id = act.idActividad;
    this.loading.set(true);
    this.error.set(null);
    forkJoin({
      regs: this.asistencias.listarPorActividad(id).pipe(catchError(() => of([] as Asistencia[]))),
      insc: this.inscripcionesSrv.listar(id).pipe(catchError(() => of([] as number[]))),
      pers: this.personasSrv.listar({ estado: true }).pipe(catchError(() => of([] as Persona[]))),
      det: this.agenda.obtener(id).pipe(catchError(() => of(null))),
    }).subscribe({
      next: ({ regs, insc, pers, det }) => {
        this.items.set(regs);
        this.inscritos.set(insc ?? []);
        this.personas.set(pers ?? []);
        this.actividadDetalle.set(det);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 0
            ? 'No se pudo conectar con el servidor.'
            : 'No se pudo cargar las asistencias.'
        );
        this.loading.set(false);
      },
    });
  }

  /** Registro manual de una persona con GPS obligatorio (sin coords manuales). */
  registrarManual(idPersona: number): void {
    const act = this.actividadCtrl.value;
    if (!act?.idActividad || this.registrando().includes(idPersona)) return;
    if (!this.gpsListo()) {
      this.error.set(this.geo.error() ?? 'Ubicación GPS obligatoria: otorga permiso y pulsa Detectar ubicación.');
      return;
    }
    const u = this.geo.ubicacion()!;
    this.registrando.update((ids) => [...ids, idPersona]);
    this.error.set(null);
    this.asistencias
      .registrarManual({ idActividad: act.idActividad, idPersona, tipoAsistencia: 'SIMPLE', latitud: u.latitud, longitud: u.longitud })
      .subscribe({
        next: () => {
          this.registrando.update((ids) => ids.filter((x) => x !== idPersona));
          this.cargar();
        },
        error: (err: HttpErrorResponse) => {
          this.registrando.update((ids) => ids.filter((x) => x !== idPersona));
          this.error.set(err.error?.message ?? 'No se pudo registrar la asistencia manual.');
        },
      });
  }

  /** Registro manual de todos los pendientes con GPS obligatorio. */
  registrarPendientes(): void {
    const act = this.actividadCtrl.value;
    const pendientes = this.filas().filter((f) => !f.asistencia);
    if (!act?.idActividad || pendientes.length === 0 || this.registrandoTodos()) return;
    if (!this.gpsListo()) {
      this.error.set(this.geo.error() ?? 'Ubicación GPS obligatoria: otorga permiso y pulsa Detectar ubicación.');
      return;
    }
    const u = this.geo.ubicacion()!;
    this.confirmar
      .confirmar(`¿Registrar asistencia manual a ${pendientes.length} pendiente(s)?`, 'Registrar todos')
      .subscribe((ok) => {
        if (!ok) return;
        this.registrandoTodos.set(true);
        this.error.set(null);
        forkJoin(
          pendientes.map((f) =>
            this.asistencias
              .registrarManual({ idActividad: act.idActividad, idPersona: f.idPersona, tipoAsistencia: 'SIMPLE', latitud: u.latitud, longitud: u.longitud })
              .pipe(catchError(() => of({ __fallo: f.idPersona } as const)))
          )
        ).subscribe((resultados) => {
          const fallos = resultados.filter((r) => '__fallo' in (r as object));
          this.registrandoTodos.set(false);
          if (fallos.length > 0) {
            this.error.set(`Se registraron ${pendientes.length - fallos.length}/${pendientes.length}. ${fallos.length} fallaron (posible duplicado).`);
          }
          this.cargar();
        });
      });
  }

  forzarCierre(): void {
    const act = this.actividadCtrl.value;
    if (!act?.idActividad) return;
    this.confirmar
      .confirmar(
        `¿Forzar el cierre de asistencias? Se marcará AUSENTE a los faltantes.`,
        'Forzar cierre'
      )
      .subscribe((ok) => {
        if (!ok) return;
        this.cerrando.set(true);
        this.asistencias.forzarCierre(act.idActividad).subscribe({
          next: (c) => {
            this.cierre.set(c);
            this.cerrando.set(false);
            this.cargar();
          },
          error: () => {
            this.error.set('No se pudo forzar el cierre.');
            this.cerrando.set(false);
          },
        });
      });
  }

  protected iniciales(idPersona: number): string {
    return `P${idPersona}`;
  }

  protected aparienciaEstado(estado: string): string {
    switch (estado) {
      case 'ASISTIO':
        return 'positive';
      case 'TARDE':
        return 'warning';
      case 'AUSENTE':
        return 'negative';
      case 'EXONERADO':
        return 'neutral';
      default:
        return 'warning';
    }
  }
}