import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  TuiButton,
  TuiDropdown, TuiFilterByInputPipe,
  TuiError,
  TuiIcon,
  TuiInput,
  TuiLabel,
  TuiTextfield,
  tuiItemsHandlersProvider,
} from '@taiga-ui/core';
import {
  TuiAvatar,
  TuiBadge,
  TuiButtonLoading,
  TuiChevron,
  TuiComboBox,
  TuiDataListWrapper,
  TuiTabs,
} from '@taiga-ui/kit';
import { EMPTY, Subscription, catchError, forkJoin, interval, of, retry } from 'rxjs';
import { AsistenciaService } from '../asistencia-service';
import { ActividadService, QrActividadService } from '../reporte-qr-actividad-service';
import { QrStreamService } from '../qr/qr-stream-service';
import { InscripcionService } from '../../actividades/agenda-service';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { GeolocalizacionService } from '../../../../core/services/geolocalizacion-service';
import { BorradorMarcacionService } from '../../../../core/services/borrador-marcacion-service';
import { TuiDialogService } from '@taiga-ui/core';
import { PolymorpheusComponent } from '@taiga-ui/polymorpheus';
import {
  CambioMarca,
  GuardarMarcacionModal,
  ResultadoGuardado,
} from './guardar-marcacion-modal';
import { CompactaList } from '../../../../shared/vistas-marcas/compacta-list';
import { TarjetaList } from '../../../../shared/vistas-marcas/tarjeta-list';
import { KanbanBoard } from '../../../../shared/vistas-marcas/kanban-board';
import {
  FilaMarca,
  VISTAS_MARCAS,
  VistaMarcas,
  guardarVista,
  leerVistaGuardada,
  siguienteMarcaDe,
} from '../../../../shared/vistas-marcas/estado-marca';
import { PersonaService } from '../../personal/persona/persona-service';
import { Actividad, Asistencia, EstadoAsistencia, MarcaAsistencia, QrActividad, TipoAsistencia } from '../models';
import { Persona } from '../../personal/persona/persona.model';

/**
 * Wizard de marcación /nuevo: paso 1 actividad (solo EN_CURSO) + GPS,
 * modos Manual (principal), Escanear QR y Mostrar QR.
 */
@Component({
  selector: 'app-asistencia-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    CompactaList,
    KanbanBoard,
    TarjetaList,
    TuiBadge,
    TuiButton,
    TuiButtonLoading,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown, TuiFilterByInputPipe,
    TuiError,
    TuiIcon,
    TuiInput,
    TuiLabel,
    TuiTabs,
    TuiTextfield,
  ],
  templateUrl: './asistencia-form.html',
  styleUrl: './asistencia-form.css',
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal(
        (item: unknown): string => {
          const o = item as { nombre?: string; codigo?: string } | null;
          return o?.nombre ?? o?.codigo ?? (typeof item === 'string' ? item : '');
        }
      ),
      identityMatcher: signal(
        (a: unknown, b: unknown): boolean =>
          ((a as { idActividad?: unknown; codigo?: unknown } | null)?.idActividad ??
            (a as { codigo?: unknown } | null)?.codigo ?? a ?? null) ===
          ((b as { idActividad?: unknown; codigo?: unknown } | null)?.idActividad ??
            (b as { codigo?: unknown } | null)?.codigo ?? b ?? null)
      ),
    }),
  ],
})
export class AsistenciaForm implements OnInit, OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly asistencias = inject(AsistenciaService);
  private readonly actividadesSrv = inject(ActividadService);
  private readonly inscripcionesSrv = inject(InscripcionService);
  private readonly qrSrv = inject(QrActividadService);
  private readonly qrStream = inject(QrStreamService);
  private readonly personasSrv = inject(PersonaService);
  private readonly confirmar = inject(ConfirmService);
  protected readonly geo = inject(GeolocalizacionService);
  private readonly borradorSrv = inject(BorradorMarcacionService);
  private readonly dialogs = inject(TuiDialogService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly tabActivo = signal(0);
  protected readonly actividades = signal<Actividad[]>([]);
  protected readonly inscritos = signal<number[]>([]);
  protected readonly personas = signal<Persona[]>([]);
  protected readonly regs = signal<Asistencia[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly exito = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly cargandoListas = signal(false);
  protected readonly guardandoGps = signal(false);
  /** Borrador local por persona (caché web). Nada viaja al backend hasta Guardar. */
  protected readonly borrador = signal<Record<number, MarcaAsistencia>>({});
  protected readonly borradorRestaurado = signal(false);

  // Mostrar QR (vivo)
  protected readonly qrActual = signal<QrActividad | null>(null);
  protected readonly qrRestante = signal<number | null>(null);
  protected readonly qrVivo = signal(false);
  protected readonly qrCargando = signal(false);
  protected readonly qrRotando = signal(false);
  protected readonly qrFinalizado = signal(false);
  private readonly qrRecibidoEn = signal<number | null>(null);
  private readonly qrUltimoSse = signal<number | null>(null);
  private vivoQr: Subscription | null = null;
  private sseCtrl: AbortController | null = null;

  protected readonly tipos: TipoAsistencia[] = ['CHECK_IN', 'CHECK_OUT', 'SIMPLE'];

  protected readonly form = this.fb.nonNullable.group({
    actividad: [null as Actividad | null, [Validators.required]],
    tipoAsistencia: ['SIMPLE' as TipoAsistencia, [Validators.required]],
    codigoQrPersonal: [''],
    codigoQrActividad: [''],
  });

  /** Solo EN_CURSO se puede marcar. */
  protected readonly actividadesMarcables = computed(() =>
    this.actividades().filter((a) => a.estado === 'EN_CURSO')
  );

  protected readonly filas = computed<FilaMarca[]>(() => {
    const porPersona = new Map<number, Asistencia>();
    for (const a of this.regs()) {
      if (!porPersona.has(a.idPersona)) porPersona.set(a.idPersona, a);
    }
    const nombres = new Map<number, string>();
    for (const p of this.personas()) {
      nombres.set(p.id, [p.apellidoPaterno, p.apellidoMaterno, p.nombres].filter(Boolean).join(' '));
    }
    const esperados = [...new Set(this.inscritos())];
    const b = this.borrador();
    const armar = (id: number): FilaMarca => {
      const srv = porPersona.get(id) ?? null;
      const marca = b[id] ?? null;
      const visible = marca != null ? this.marcaAEstado(marca) : (srv?.estado ?? null);
      return {
        idPersona: id,
        nombre: nombres.get(id) ?? `Persona #${id}`,
        asistencia: srv,
        visible,
        modificado: marca != null && !this.equivale(srv?.estado ?? null, marca),
      };
    };
    const filas: FilaMarca[] = esperados.map(armar);
    for (const id of porPersona.keys()) {
      if (!esperados.includes(id)) filas.push(armar(id));
    }
    for (const key of Object.keys(b)) {
      const id = Number(key);
      if (!esperados.includes(id) && !porPersona.has(id)) filas.push(armar(id));
    }
    return filas.sort(
      (x, y) => Number(x.visible == null) - Number(y.visible == null) || x.nombre.localeCompare(y.nombre)
    );
  });

  /** Cambios reales del borrador (excluye equivalencias con el servidor). */
  protected readonly cambios = computed<CambioMarca[]>(() => {
    const b = this.borrador();
    const nombres = new Map(this.filas().map((f) => [f.idPersona, f.nombre] as const));
    const srv = new Map(this.regs().map((a) => [a.idPersona, a.estado] as const));
    const out: CambioMarca[] = [];
    for (const [key, marca] of Object.entries(b)) {
      const id = Number(key);
      const de = srv.get(id) ?? null;
      if (!this.equivale(de, marca)) {
        out.push({ idPersona: id, nombre: nombres.get(id) ?? `Persona #${id}`, de, a: marca });
      }
    }
    return out.sort((x, y) => x.nombre.localeCompare(y.nombre));
  });

  protected readonly resumen = computed(() => {
    const filas = this.filas();
    const esp = this.esperadosIds().length;
    const presentes = filas.filter((f) => f.visible === 'ASISTIO').length;
    const tardes = filas.filter((f) => f.visible === 'TARDE').length;
    const faltas = filas.filter((f) => f.visible === 'AUSENTE').length;
    const pendientes = filas.filter((f) => f.visible == null).length;
    return {
      esperados: esp,
      presentes,
      tardes,
      faltas,
      pendientes,
      sinGuardar: this.cambios().length,
      porcentaje: esp > 0 ? Math.round(((presentes + tardes) * 100) / esp) : 0,
    };
  });

  private marcaAEstado(m: MarcaAsistencia): EstadoAsistencia {
    return m === 'PRESENTE' ? 'ASISTIO' : m === 'TARDE' ? 'TARDE' : 'AUSENTE';
  }

  private equivale(de: EstadoAsistencia | null, marca: MarcaAsistencia): boolean {
    if (de == null) return false;
    return this.marcaAEstado(marca) === de;
  }

  private esperadosIds(): number[] {
    return [...new Set(this.inscritos())];
  }

  ngOnInit(): void {
    const modo = this.route.snapshot.queryParamMap.get('modo');
    if (modo === 'qr') this.tabActivo.set(1);
    if (modo === 'mostrar') this.tabActivo.set(2);
    this.loading.set(true);
    forkJoin({
      acts: this.actividadesSrv.listar().pipe(catchError(() => of([]))),
      pers: this.personasSrv.listar({ estado: true }).pipe(catchError(() => of([]))),
    }).subscribe(({ acts, pers }) => {
      this.actividades.set(acts);
      this.personas.set(pers);
      this.loading.set(false);
      this.geo.detectar().subscribe({ next: () => undefined, error: () => undefined });
    });
    this.form.controls.actividad.valueChanges.subscribe((act) => {
      this.exito.set(null);
      this.error.set(null);
      this.qrActual.set(null);
      this.limpiarVivoQr();
      if (act?.idActividad) {
        // Restaura borrador de caché (no toca backend).
        const cache = this.borradorSrv.cargar(act.idActividad);
        this.borrador.set(cache);
        this.borradorRestaurado.set(Object.keys(cache).length > 0);
        this.cargarListas(act.idActividad);
      } else {
        this.borrador.set({});
        this.borradorRestaurado.set(false);
        this.inscritos.set([]);
        this.regs.set([]);
      }
    });
  }

  ngOnDestroy(): void {
    this.limpiarVivoQr();
  }

  protected nombreActividad(a: Actividad): string {
    return a.nombre;
  }

  // ---------------- GPS ----------------

  protected gpsListo(): boolean {
    return this.geo.estado() === 'ok' && this.geo.ubicacion() != null;
  }

  protected detectarUbicacion(): void {
    this.error.set(null);
    this.geo.detectar().subscribe({ next: () => undefined, error: () => this.error.set(this.geo.error()) });
  }

  protected enlaceMiUbicacion(): string | null {
    const u = this.geo.ubicacion();
    return u ? GeolocalizacionService.enlaceGoogleMaps(u.latitud, u.longitud) : null;
  }

  private coords(): { latitud: number; longitud: number } | null {
    const u = this.geo.ubicacion();
    return this.gpsListo() && u ? { latitud: u.latitud, longitud: u.longitud } : null;
  }

  // ---------------- Manual: bulk + bucle ----------------

  private cargarListas(idActividad: number): void {
    this.cargandoListas.set(true);
    forkJoin({
      insc: this.inscripcionesSrv.listar(idActividad).pipe(catchError(() => of([] as number[]))),
      regs: this.asistencias.listarPorActividad(idActividad).pipe(catchError(() => of([] as Asistencia[]))),
    }).subscribe(({ insc, regs }) => {
      this.inscritos.set(insc ?? []);
      this.regs.set(regs ?? []);
      this.cargandoListas.set(false);
    });
  }

  private recargarRegs(): void {
    const act = this.form.controls.actividad.value;
    if (!act?.idActividad) return;
    this.asistencias
      .listarPorActividad(act.idActividad)
      .pipe(catchError(() => of([] as Asistencia[])))
      .subscribe((regs) => this.regs.set(regs ?? []));
  }

  /** Vista de lista (tarjetas|compacta|kanban). Compacta por defecto, luego la última usada. */
  protected readonly vista = signal<VistaMarcas>(leerVistaGuardada());
  protected readonly vistas = VISTAS_MARCAS;

  protected cambiarVista(v: VistaMarcas): void {
    this.vista.set(v);
    guardarVista(v);
  }

  /** Tap/bucle desde las vistas hijas: solo muta el borrador local (caché web). */
  alCiclar(idPersona: number): void {
    const act = this.form.controls.actividad.value;
    if (!act?.idActividad) return;
    const f = this.filas().find((x) => x.idPersona === idPersona);
    if (!f) return;
    const marca = siguienteMarcaDe(f.visible);
    if (!marca) return;
    this.borrador.update((b) => {
      const next = { ...b };
      if (this.equivale(f.asistencia?.estado ?? null, marca)) delete next[idPersona];
      else next[idPersona] = marca;
      return next;
    });
    this.persistirBorrador(act.idActividad);
    this.exito.set(null);
  }

  /** Marca directa (segmento de tarjetas, drag-drop de kanban). */
  alMarcarA(e: { idPersona: number; marca: MarcaAsistencia }): void {
    const act = this.form.controls.actividad.value;
    if (!act?.idActividad) return;
    const f = this.filas().find((x) => x.idPersona === e.idPersona);
    this.borrador.update((b) => {
      const next = { ...b };
      if (f && this.equivale(f.asistencia?.estado ?? null, e.marca)) delete next[e.idPersona];
      else next[e.idPersona] = e.marca;
      return next;
    });
    this.persistirBorrador(act.idActividad);
    this.exito.set(null);
  }

  /** Marcar asistencia: todos los pendientes a PRESENTE en borrador. */
  marcarTodos(): void {
    const act = this.form.controls.actividad.value;
    if (!act?.idActividad) return;
    const pendientes = this.filas().filter((f) => f.visible == null);
    if (pendientes.length === 0) return;
    this.borrador.update((b) => {
      const next = { ...b };
      for (const f of pendientes) next[f.idPersona] = 'PRESENTE';
      return next;
    });
    this.persistirBorrador(act.idActividad);
    this.exito.set(null);
  }

  /** Descarta el borrador (caché + vista). */
  descartarBorrador(): void {
    const act = this.form.controls.actividad.value;
    if (!act?.idActividad) return;
    this.borrador.set({});
    this.borradorSrv.limpiar(act.idActividad);
    this.borradorRestaurado.set(false);
  }

  private persistirBorrador(idActividad: number): void {
    this.borradorSrv.guardar(idActividad, this.borrador());
  }

  /**
   * Guardar: re-captura GPS fresco y abre el modal de confirmación.
   * Solo aquí viaja algo al backend.
   */
  guardarBorrador(): void {
    const act = this.form.controls.actividad.value;
    const cambios = this.cambios();
    if (!act?.idActividad || cambios.length === 0 || this.guardandoGps()) return;
    this.error.set(null);
    this.guardandoGps.set(true);
    this.geo.detectar().subscribe({
      next: (u) => {
        this.guardandoGps.set(false);
        this.dialogs
          .open<ResultadoGuardado | null>(new PolymorpheusComponent(GuardarMarcacionModal), {
            label: `Guardar ${cambios.length} cambio(s) — ${act.nombre}`,
            data: {
              idActividad: act.idActividad,
              actividadNombre: act.nombre,
              tipoAsistencia: this.form.controls.tipoAsistencia.value,
              latitud: u.latitud,
              longitud: u.longitud,
              cambios,
            },
          })
          .subscribe((res) => this.reconciliarGuardado(act.idActividad, res));
      },
      error: () => {
        this.guardandoGps.set(false);
        this.error.set(this.geo.error() ?? 'Activa el GPS para guardar.');
      },
    });
  }

  /** Quita del borrador lo guardado; conserva fallidos; recarga servidor. */
  private reconciliarGuardado(idActividad: number, res: ResultadoGuardado | null | undefined): void {
    if (!res) return;
    const idsFallo = new Set(res.fallidos.map((f) => f.idPersona));
    this.borrador.update((b) => {
      const next = { ...b };
      for (const id of res.exitosos) delete next[id];
      return next;
    });
    this.persistirBorrador(idActividad);
    this.recargarRegs();
    if (res.fallidos.length > 0) {
      this.error.set(
        `Guardados ${res.exitosos.length}/${res.exitosos.length + res.fallidos.length}. Revisa los fallidos en el borrador.`
      );
    } else {
      this.exito.set(`Guardado completo: ${res.exitosos.length} cambio(s).`);
    }
  }

  // ---------------- Escanear QR ----------------

  registrarPorQr(): void {
    const c = this.form.controls;
    const actividad = c.actividad.value;
    const u = this.coords();
    if (!actividad) {
      this.error.set('Selecciona la actividad EN_CURSO.');
      return;
    }
    if (!c.codigoQrPersonal.value?.trim() || !c.codigoQrActividad.value?.trim()) {
      this.error.set('Ingresa el QR personal y el QR de actividad.');
      return;
    }
    if (!u) {
      this.error.set(this.geo.error() ?? 'Ubicación GPS obligatoria.');
      return;
    }
    this.confirmar
      .confirmar(`¿Registrar asistencia por QR en ${actividad.nombre}?`, 'Registrar')
      .subscribe((ok) => {
        if (!ok) return;
        this.error.set(null);
        this.asistencias
          .registrarPorQr({
            idActividad: actividad.idActividad,
            codigoQrPersonal: c.codigoQrPersonal.value.trim(),
            codigoQrActividad: c.codigoQrActividad.value.trim(),
            tipoAsistencia: c.tipoAsistencia.value,
            latitud: u.latitud,
            longitud: u.longitud,
          })
          .subscribe({
            next: () => {
              this.exito.set('Asistencia por QR registrada.');
              c.codigoQrPersonal.setValue('');
              c.codigoQrActividad.setValue('');
              this.recargarRegs();
            },
            error: (err: HttpErrorResponse) => {
              this.error.set(
                err.status === 409
                  ? (err.error?.message ?? 'QR inválido o duplicado.')
                  : (err.error?.message ?? 'No se pudo registrar.')
              );
            },
          });
      });
  }

  // ---------------- Mostrar QR (vivo) ----------------

  protected cargarQr(): void {
    const act = this.form.controls.actividad.value;
    if (!act?.idActividad || this.qrCargando()) return;
    this.qrCargando.set(true);
    this.qrSrv.actual(act.idActividad).subscribe({
      next: (q) => {
        this.aplicarQr(q, false);
        this.qrCargando.set(false);
        this.iniciarVivoQr(act.idActividad);
      },
      error: () => this.qrCargando.set(false),
    });
  }

  protected rotarQr(): void {
    const act = this.form.controls.actividad.value;
    if (!act?.idActividad || this.qrRotando()) return;
    this.qrRotando.set(true);
    this.qrSrv.rotar(act.idActividad).subscribe({
      next: (q) => {
        this.aplicarQr(q, false);
        this.qrRotando.set(false);
      },
      error: () => this.qrRotando.set(false),
    });
  }

  private aplicarQr(q: QrActividad, porSse: boolean): void {
    this.qrActual.set(q);
    this.qrRecibidoEn.set(Date.now());
    this.qrRestante.set(q.expiraEnSegundos ?? null);
    if (porSse) {
      this.qrUltimoSse.set(Date.now());
      this.qrVivo.set(true);
    }
  }

  private iniciarVivoQr(id: number): void {
    this.limpiarVivoQr();
    this.sseCtrl = new AbortController();
    const vivo = new Subscription();
    vivo.add(
      this.qrStream
        .stream(id, this.sseCtrl.signal)
        .pipe(
          retry({ delay: 10000 }),
          catchError(() => EMPTY)
        )
        .subscribe({
          next: (q) => this.aplicarQr(q, true),
          complete: () => {
            this.qrFinalizado.set(true);
            this.limpiarVivoQr();
          },
        })
    );
    vivo.add(
      interval(30000).subscribe(() => {
        const ultimo = this.qrUltimoSse();
        if (ultimo != null && Date.now() - ultimo < 45000) return;
        this.qrVivo.set(false);
        this.qrSrv.actual(id).subscribe({
          next: (q) => this.aplicarQr(q, false),
          error: () => undefined,
        });
      })
    );
    vivo.add(
      interval(1000).subscribe(() => {
        const r = this.qrRecibidoEn();
        const a = this.qrActual();
        if (r == null || a?.expiraEnSegundos == null) {
          this.qrRestante.set(null);
          return;
        }
        this.qrRestante.set(Math.max(0, a.expiraEnSegundos - Math.floor((Date.now() - r) / 1000)));
      })
    );
    this.vivoQr = vivo;
  }

  private limpiarVivoQr(): void {
    this.vivoQr?.unsubscribe();
    this.vivoQr = null;
    this.sseCtrl?.abort();
    this.sseCtrl = null;
    this.qrVivo.set(false);
    this.qrFinalizado.set(false);
  }

  protected formatoRestante(): string {
    const r = this.qrRestante();
    if (r == null) return '—';
    if (r === 0) return 'Rotando…';
    return `${Math.floor(r / 60)}:${String(r % 60).padStart(2, '0')}`;
  }

  protected nombrePersona(p: Persona): string {
    return [p.apellidoPaterno, p.apellidoMaterno, p.nombres].filter((x) => !!x).join(' ');
  }

  volver(): void {
    this.router.navigate(['/asistencias/registro']);
  }
}
