import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  TuiButton,
  TuiError,
  TuiIcon,
  TuiInput,
  TuiLabel,
  TuiTextfield,
} from '@taiga-ui/core';
import { TuiAvatar, TuiBadge, TuiButtonLoading, TuiTabs, TuiTextarea } from '@taiga-ui/kit';
import { AptitudService } from '../aptitud-service';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { EvaluacionAptitud } from '../models';

import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-evaluacion-detail',
  imports: [
    CommonModule,
    RouterLink,
    ReactiveFormsModule,
    TuiAvatar,
    TuiBadge,
    TuiButton,
    TuiButtonLoading,
    TuiError,
    TuiIcon,
    TuiInput,
    TuiLabel,
    TuiTabs,
    TuiTextarea,
    TuiTextfield,
  ],
  templateUrl: './evaluacion-detail.html',
  styleUrl: './evaluacion-detail.css',
})
export class EvaluacionDetail implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(AptitudService);
  private readonly confirmar = inject(ConfirmService);
  private readonly route = inject(ActivatedRoute);
  protected readonly router = inject(Router);

  protected readonly tabActivo = signal(0);
  protected readonly evaluacion = signal<EvaluacionAptitud | null>(null);
  protected readonly loading = signal(false);
  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly mensajeExito = signal<string | null>(null);
  protected readonly editandoObs = signal(false);

  protected readonly tieneFuerza = computed(() => !!this.evaluacion()?.detalleFuerza);
  protected readonly tieneCardio = computed(() => !!this.evaluacion()?.detalleCardio);
  protected readonly soloLectura = computed(
    () => this.evaluacion()?.estadoEvaluacion === 'ANULADA',
  );

  protected readonly fuerzaForm = this.fb.nonNullable.group({
    repeticionesAbdominales: [null as number | null, [Validators.required, Validators.min(0), Validators.max(100)]],
    repeticionesFlexionBrazo: [null as number | null, [Validators.required, Validators.min(0), Validators.max(100)]],
    saltoLongitudCm: [null as number | null, [Validators.required, Validators.min(0), Validators.max(400)]],
  });

  protected readonly cardioForm = this.fb.nonNullable.group({
    repeticionesSoga30s: [null as number | null, [Validators.required, Validators.min(0), Validators.max(200)]],
    tiempoCarrera400mSeg: [null as number | null, [Validators.required, Validators.min(30), Validators.max(900)]],
  });

  protected readonly obsForm = this.fb.nonNullable.group({
    observaciones: ['', [Validators.maxLength(500)]],
  });

  private id = 0;

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const idParam = params.get('id');
      if (!idParam) {
        this.router.navigate(['/aptitud']);
        return;
      }
      this.id = Number(idParam);
      this.evaluacion.set(null);
      this.cargar();
    });
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.obtener(this.id).subscribe({
      next: (e) => {
        this.evaluacion.set(e);
        if (e.detalleFuerza) {
          this.fuerzaForm.patchValue({
            repeticionesAbdominales: e.detalleFuerza.repeticionesAbdominales ?? null,
            repeticionesFlexionBrazo: e.detalleFuerza.repeticionesFlexionBrazo ?? null,
            saltoLongitudCm: e.detalleFuerza.saltoLongitudCm ?? null,
          });
        }
        if (e.detalleCardio) {
          this.cardioForm.patchValue({
            repeticionesSoga30s: e.detalleCardio.repeticionesSoga30s ?? null,
            tiempoCarrera400mSeg: e.detalleCardio.tiempoCarrera400mSeg ?? null,
          });
        }
        this.obsForm.patchValue({ observaciones: e.observacionesRestricciones ?? '' });
        this.editarFormularios(e.estadoEvaluacion !== 'ANULADA');
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(err.status === 404 ? 'Evaluación no encontrada.' : 'No se pudo cargar la evaluación.');
        this.loading.set(false);
      },
    });
  }

  guardarFuerza(): void {
    if (this.soloLectura() || this.fuerzaForm.invalid) {
      this.fuerzaForm.markAllAsTouched();
      return;
    }
    const v = this.fuerzaForm.getRawValue();
    const payload = {
      repeticionesAbdominales: Number(v.repeticionesAbdominales),
      repeticionesFlexionBrazo: Number(v.repeticionesFlexionBrazo),
      saltoLongitudCm: Number(v.saltoLongitudCm),
    };
    const actualizando = this.tieneFuerza();
    this.guardando.set(true);
    this.error.set(null);
    this.mensajeExito.set(null);
    const peticion = actualizando
      ? this.service.actualizarFuerza(this.id, payload)
      : this.service.registrarFuerza(this.id, payload);
    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.mensajeExito.set(
          actualizando
            ? 'Prueba de fuerza actualizada exitosamente.'
            : 'Prueba de fuerza guardada exitosamente.',
        );
        this.cargar();
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.error?.message ??
            (actualizando ? 'No se pudo actualizar la fuerza.' : 'No se pudo registrar la fuerza.'),
        );
        this.guardando.set(false);
      },
    });
  }

  guardarCardio(): void {
    if (this.soloLectura() || this.cardioForm.invalid) {
      this.cardioForm.markAllAsTouched();
      return;
    }
    const v = this.cardioForm.getRawValue();
    const payload = {
      repeticionesSoga30s: Number(v.repeticionesSoga30s),
      tiempoCarrera400mSeg: Number(v.tiempoCarrera400mSeg),
    };
    const actualizando = this.tieneCardio();
    this.guardando.set(true);
    this.error.set(null);
    this.mensajeExito.set(null);
    const peticion = actualizando
      ? this.service.actualizarCardio(this.id, payload)
      : this.service.registrarCardio(this.id, payload);
    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.mensajeExito.set(
          actualizando
            ? 'Prueba cardiovascular actualizada exitosamente.'
            : 'Prueba cardiovascular guardada exitosamente.',
        );
        this.cargar();
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.error?.message ??
            (actualizando ? 'No se pudo actualizar el cardio.' : 'No se pudo registrar el cardio.'),
        );
        this.guardando.set(false);
      },
    });
  }

  guardarObservaciones(): void {
    if (this.soloLectura()) return;
    const texto = (this.obsForm.controls.observaciones.value ?? '').trim();
    if (texto.length > 500) {
      this.error.set('Máximo 500 caracteres.');
      return;
    }
    this.guardando.set(true);
    this.error.set(null);
    this.mensajeExito.set(null);
    this.service.actualizarObservaciones(this.id, texto).subscribe({
      next: () => {
        this.guardando.set(false);
        this.editandoObs.set(false);
        this.mensajeExito.set('Observaciones actualizadas exitosamente.');
        this.cargar();
      },
      error: () => {
        this.error.set('No se pudieron guardar las observaciones.');
        this.guardando.set(false);
      },
    });
  }

  anular(): void {
    const e = this.evaluacion();
    if (!e) return;
    this.confirmar
      .confirmar(`¿Anular la evaluación de ${e.nombresCompletosAlMomento ?? e.id}?`, 'Anular')
      .subscribe((ok) => {
        if (!ok) return;
        this.guardando.set(true);
        this.service.anular(e.id).subscribe({
          next: () => this.router.navigate(['/aptitud']),
          error: (err: HttpErrorResponse) => {
            this.error.set(err.error?.message ?? 'No se pudo anular.');
            this.guardando.set(false);
          },
        });
      });
  }

  private editarFormularios(editable: boolean): void {
    for (const form of [this.fuerzaForm, this.cardioForm, this.obsForm]) {
      if (editable) {
        form.enable({ emitEvent: false });
      } else {
        form.disable({ emitEvent: false });
      }
    }
  }

  protected iniciales(nombre?: string): string {
    if (!nombre) return '?';
    return nombre
      .split(' ')
      .filter((x) => x.length > 0)
      .slice(0, 2)
      .map((x) => x.charAt(0).toUpperCase())
      .join('');
  }

  protected clasificacionFitness(score?: number | null): { label: string; clase: string } {
    if (score === undefined || score === null) return { label: 'Sin evaluar', clase: 'pill-soft-neutral' };
    if (score >= 85) return { label: 'Excelente', clase: 'pill-soft-success' };
    if (score >= 70) return { label: 'Muy Bueno', clase: 'pill-soft-success' };
    if (score >= 50) return { label: 'Bueno', clase: 'pill-soft-primary' };
    if (score >= 30) return { label: 'Regular', clase: 'pill-soft-warning' };
    return { label: 'Bajo / Malo', clase: 'pill-soft-error' };
  }

  protected clasificacionDiagnostico(dx?: string | null): { label: string; clase: string } {
    if (!dx) return { label: '—', clase: 'pill-soft-neutral' };
    switch (dx.toUpperCase()) {
      case 'EXCELENTE':
        return { label: 'Excelente', clase: 'pill-soft-success' };
      case 'MUY_BUENO':
      case 'MUY BUENO':
        return { label: 'Muy Bueno', clase: 'pill-soft-success' };
      case 'BUENO':
        return { label: 'Bueno', clase: 'pill-soft-primary' };
      case 'PROMEDIO':
      case 'REGULAR':
        return { label: 'Regular', clase: 'pill-soft-warning' };
      case 'BAJO':
        return { label: 'Bajo', clase: 'pill-soft-error' };
      default:
        return { label: dx, clase: 'pill-soft-neutral' };
    }
  }

  protected aparienciaDictamen(d?: string): string {
    switch (d) {
      case 'APTO':
        return 'positive';
      case 'APTO_CON_RESTRICCIONES':
        return 'warning';
      case 'NO_APTO':
        return 'negative';
      default:
        return 'neutral';
    }
  }
}
