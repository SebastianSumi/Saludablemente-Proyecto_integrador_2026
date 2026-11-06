import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  TuiButton,
  TuiCalendar,
  TuiCheckbox,
  TuiDropdown,
  TuiError,
  TuiFilterByInputPipe,
  TuiIcon,
  TuiInput,
  TuiLabel,
  TuiTextfield,
  tuiItemsHandlersProvider,
} from '@taiga-ui/core';
import { TuiButtonLoading, TuiChevron, TuiComboBox, TuiDataListWrapper, TuiInputDate } from '@taiga-ui/kit';
import { fijarHoyEn, limpiarFechaEn } from '../../../../core/forms/fecha-transformers';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AgendaService } from '../agenda-service';
import { PeriodoService } from '../../catalogo/periodo/periodo-service';
import type { Periodo } from '../../catalogo/periodo/periodo.model';
import { MapaSede, SedePin } from '../../../../shared/components/mapa-sede/mapa-sede';
import { GeolocalizacionService } from '../../../../core/services/geolocalizacion-service';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { PerfilService } from '../../perfil/perfil-service';
import { UsuarioService } from '../../seguridad/usuario-service';
import { Usuario } from '../../seguridad/models';

@Component({
  selector: 'app-actividad-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MapaSede,
    TuiButton,
    TuiButtonLoading,
    TuiCalendar,
    TuiCheckbox,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown,
    TuiError,
    TuiFilterByInputPipe,
    TuiIcon,
    TuiInput,
    TuiInputDate,
    TuiLabel,
    TuiTextfield,
  ],
  templateUrl: './actividad-form.html',
  styleUrl: './actividad-form.css',
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal(
        (item: unknown): string => {
          const o = item as { email?: string; codigo?: string } | null;
          return o?.email ?? o?.codigo ?? (typeof item === 'string' ? (item as string) : '');
        }
      ),
      identityMatcher: signal(
        (a: unknown, b: unknown): boolean => {
          const x = a as { id?: unknown; codigo?: unknown } | null;
          const y = b as { id?: unknown; codigo?: unknown } | null;
          return (
            (x?.id ?? (x as { codigo?: unknown } | null)?.codigo ?? a ?? null) ===
            (y?.id ?? (y as { codigo?: unknown } | null)?.codigo ?? b ?? null)
          );
        }
      ),
    }),
  ],
})
export class ActividadForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(AgendaService);
  private readonly usuariosSrv = inject(UsuarioService);
  private readonly perfilSrv = inject(PerfilService);
  private readonly confirmar = inject(ConfirmService);
  private readonly periodosSrv = inject(PeriodoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly coordinadores = signal<Usuario[]>([]);
  protected readonly periodos = signal<Periodo[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly guardando = signal(false);
  protected readonly modoEdicion = signal(false);
  protected readonly sedePin = signal<SedePin | null>(null);
  protected idAutor = 0;
  private idActividad: number | null = null;

  protected readonly form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    descripcion: ['', [Validators.maxLength(300)]],
    fechaActividad: ['', [Validators.required]],
    horaInicio: ['', [Validators.required]],
    horaFin: ['', [Validators.required]],
    lugar: ['', [Validators.required, Validators.maxLength(100)]],
    coordinador: [null as Usuario | null, [Validators.required]],
    latitudOficial: [null as number | null, [Validators.required, Validators.min(-90), Validators.max(90)]],
    longitudOficial: [null as number | null, [Validators.required, Validators.min(-180), Validators.max(180)]],
    toleranciaGeocercaMetros: [100, [Validators.required, Validators.min(10), Validators.max(2000)]],
    duracionMinutos: [null as number | null],
    periodo: [null as Periodo | null, [Validators.required]],
    requiereCheckInOut: [false],
    permiteAutoservicio: [true],
    permiteEscaneoCoordinador: [true],
  });

  ngOnInit(): void {
    this.loading.set(true);
    const idParam = this.route.snapshot.paramMap.get('id');
    const id = idParam ? Number(idParam) : NaN;
    const esEdicion = Number.isInteger(id) && id > 0;
    forkJoin({
      usuarios: this.usuariosSrv.buscar({ activo: true, size: 100 }).pipe(catchError(() => of(null))),
      perfil: this.perfilSrv.obtener().pipe(catchError(() => of(null))),
      periodos: this.periodosSrv.listar().pipe(catchError(() => of([] as Periodo[]))),
      detalle: esEdicion
        ? this.service.obtener(id).pipe(catchError(() => of(null)))
        : of(null),
    }).subscribe(({ usuarios, perfil, periodos, detalle }) => {
      if (usuarios) this.coordinadores.set(usuarios.content);
      if (perfil) this.idAutor = perfil.idUsuario;
      // CERRADO no se muestra.
      const utiles = (periodos ?? []).filter((p) => p.estado !== 'CERRADO');
      this.periodos.set(utiles);
      if (!esEdicion) {
        const vigente = utiles.find((p) => p.estado === 'VIGENTE') ?? utiles[0] ?? null;
        if (vigente) this.form.controls.periodo.setValue(vigente);
      }
      if (detalle) {
        this.modoEdicion.set(true);
        this.idActividad = detalle.idActividad;
        this.form.patchValue({
          nombre: detalle.nombre ?? '',
          descripcion: (detalle as { descripcion?: string }).descripcion ?? '',
          fechaActividad: detalle.fechaActividad ?? '',
          horaInicio: detalle.horaInicio ?? '',
          horaFin: detalle.horaFin ?? '',
          lugar: detalle.lugar ?? '',
          latitudOficial: detalle.latitudOficial ?? null,
          longitudOficial: detalle.longitudOficial ?? null,
          toleranciaGeocercaMetros: detalle.toleranciaGeocercaMetros ?? 100,
          duracionMinutos: detalle.duracionMinutos ?? null,
          periodo: this.periodos().find((p) => p.codigo === detalle.periodo) ?? null,
          requiereCheckInOut: detalle.requiereCheckInOut ?? false,
          permiteAutoservicio: detalle.permiteAutoservicio ?? true,
          permiteEscaneoCoordinador: detalle.permiteEscaneoCoordinador ?? true,
        });
        if (detalle.latitudOficial != null && detalle.longitudOficial != null) {
          this.sedePin.set({ latitud: detalle.latitudOficial, longitud: detalle.longitudOficial });
        }
      }
      this.loading.set(false);
    });
  }

  protected onSedeChange(pin: SedePin): void {
    this.sedePin.set(pin);
    this.form.patchValue({ latitudOficial: pin.latitud, longitudOficial: pin.longitud });
    this.form.controls.latitudOficial.markAsTouched();
    this.form.controls.longitudOficial.markAsTouched();
  }

  protected enlaceSede(): string | null {
    const lat = this.form.controls.latitudOficial.value;
    const lon = this.form.controls.longitudOficial.value;
    return lat != null && lon != null
      ? GeolocalizacionService.enlaceGoogleMaps(lat, lon)
      : null;
  }

  protected nombreUsuario(u: Usuario): string {
    return u.email;
  }

  guardar(): void {
    if (this.loading() || this.guardando()) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set(
        this.form.controls.latitudOficial.invalid || this.form.controls.longitudOficial.invalid
          ? 'Fija la sede en el mapa (obligatoria).'
          : 'Completa los campos obligatorios.'
      );
      return;
    }
    if (!this.idAutor) {
      this.error.set('No se pudo identificar al autor. Recarga la página.');
      return;
    }
    const v = this.form.getRawValue();
    if (v.horaInicio >= v.horaFin) {
      this.error.set('La hora de inicio debe ser anterior a la de fin.');
      return;
    }
    const coordinador = v.coordinador as Usuario;
    const periodo = v.periodo as Periodo | null;
    if (!periodo) {
      this.error.set('Selecciona el periodo.');
      return;
    }
    // Fecha dentro del rango del periodo (bloqueante).
    if (periodo.fechaInicio && periodo.fechaFin && v.fechaActividad) {
      if (v.fechaActividad < periodo.fechaInicio || v.fechaActividad > periodo.fechaFin) {
        this.error.set(
          `La fecha no está dentro del periodo ${periodo.codigo} (${periodo.fechaInicio} - ${periodo.fechaFin}).`
        );
        return;
      }
    }
    const lat = Number(v.latitudOficial);
    const lon = Number(v.longitudOficial);
    if (!Number.isFinite(lat) || !Number.isFinite(lon)) {
      this.error.set('Fija la sede en el mapa (obligatoria).');
      return;
    }
    // Modo edición (viejas sin sede incluidas): solo actualiza sede + radio.
    if (this.modoEdicion() && this.idActividad != null) {
      const id = this.idActividad;
      this.confirmar.confirmar('¿Guardar la sede y el radio de geocerca?', 'Guardar').subscribe((ok) => {
        if (!ok) return;
        this.guardando.set(true);
        this.error.set(null);
        this.service
          .actualizarSede(id, {
            latitudOficial: lat,
            longitudOficial: lon,
            toleranciaGeocercaMetros: Number(v.toleranciaGeocercaMetros) || 100,
          })
          .subscribe({
            next: (a) => this.router.navigate(['/actividades', a.idActividad]),
            error: (err: HttpErrorResponse) => {
              this.error.set(err.error?.message ?? 'No se pudo guardar la sede.');
              this.guardando.set(false);
            },
          });
      });
      return;
    }
    this.confirmar
      .confirmar(`¿Crear la actividad "${(v.nombre ?? '').trim()}"?`, 'Crear')
      .subscribe((ok) => {
        if (!ok) return;
        this.guardando.set(true);
        this.error.set(null);
        this.service
          .crear({
            nombre: (v.nombre ?? '').trim(),
            descripcion: (v.descripcion ?? '').trim() || undefined,
            fechaActividad: v.fechaActividad,
            horaInicio: v.horaInicio,
            horaFin: v.horaFin,
            lugar: (v.lugar ?? '').trim(),
            idUsuarioCreador: this.idAutor,
            idUsuarioCoordinador: coordinador.id,
            latitudOficial: lat,
            longitudOficial: lon,
            toleranciaGeocercaMetros: Number(v.toleranciaGeocercaMetros) || 100,
            duracionMinutos: v.duracionMinutos ?? undefined,
            periodo: periodo.codigo,
            requiereCheckInOut: v.requiereCheckInOut,
            permiteAutoservicio: v.permiteAutoservicio,
            permiteEscaneoCoordinador: v.permiteEscaneoCoordinador,
          })
          .subscribe({
            next: (a) => this.router.navigate(['/actividades', a.idActividad]),
            error: (err: HttpErrorResponse) => {
              this.error.set(err.error?.message ?? 'No se pudo crear la actividad.');
              this.guardando.set(false);
            },
          });
      });
  }

  cancelar(): void {
    this.router.navigate(['/actividades']);
  }

  protected fijarHoy(campo: string): void {
    fijarHoyEn(this.form.get(campo));
  }

  protected limpiarCampo(campo: string): void {
    limpiarFechaEn(this.form.get(campo));
  }
}
