import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TuiButton, TuiIcon, TuiInput, TuiLabel, TuiTextfield } from '@taiga-ui/core';
import { TuiButtonLoading } from '@taiga-ui/kit';
import { AntropometricoService } from './antropometrico-service';
import { EvaluacionService } from '../evaluacion/evaluacion-service';
import { EvaluacionResponse } from '../evaluacion/evaluacion.model';

import { AntropometricoRequest } from './antropometrico.model';

@Component({
  selector: 'app-antropometrico-form',
  imports: [ReactiveFormsModule, RouterLink, TuiButton, TuiButtonLoading, TuiIcon, TuiInput, TuiLabel, TuiTextfield],
  templateUrl: './antropometrico-form.html',
  styleUrl: './antropometrico-form.css',
})
export class AntropometricoForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly antropometricoService = inject(AntropometricoService);
  private readonly evaluacionService = inject(EvaluacionService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly evaluacion = signal<EvaluacionResponse | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly errorCarga = signal(false);
  protected readonly modoEdicion = signal(false);
  protected readonly guardando = signal(false);

  protected idEvaluacion!: number;

  protected readonly form = this.fb.nonNullable.group({
    estaturaCm: [0, [
      Validators.required,
      Validators.min(100),
      Validators.max(250),
    ]],
    pesoKg: [0, [
      Validators.required,
      Validators.min(20),
      Validators.max(300),
    ]],
    perimetroAbdominalCm: [null as number | null, [
      Validators.min(40),
      Validators.max(200),
    ]],
    porcentajeGrasa: [null as number | null, [
      Validators.min(0),
      Validators.max(70),
    ]],
    porcentajeMasaMuscular: [null as number | null, [
      Validators.min(0),
      Validators.max(100),
    ]],
    porcentajeGrasaVisceral: [null as number | null, [
      Validators.min(0),
      Validators.max(30),
    ]],
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      this.error.set('ID de evaluación no válido.');
      return;
    }
    this.idEvaluacion = Number(idParam);
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);

    this.evaluacionService.obtener(this.idEvaluacion).subscribe({
      next: (evaluacion) => {
        this.evaluacion.set(evaluacion);

        if (evaluacion.estadoEvaluacion === 'ANULADA') {
          this.errorCarga.set(true);
          this.error.set('No se puede modificar una evaluación anulada.');
          this.loading.set(false);
          return;
        }

        const antro = evaluacion.detalleAntropometrico;
        if (antro) {
          this.modoEdicion.set(true);
          this.form.patchValue({
            estaturaCm: antro.estaturaCm,
            pesoKg: antro.pesoKg,
            perimetroAbdominalCm: antro.perimetroAbdominalCm ?? null,
            porcentajeGrasa: antro.porcentajeGrasa ?? null,
            porcentajeMasaMuscular: antro.porcentajeMasaMuscular ?? null,
            porcentajeGrasaVisceral: antro.porcentajeGrasaVisceral ?? null,
          });
        }
        this.loading.set(false);
      },
      error: () => {
        this.errorCarga.set(true);
        this.error.set('No se pudo cargar la evaluación.');
        this.loading.set(false);
      },
    });
  }

  guardar(): void {
    if (this.loading() || this.errorCarga() || this.guardando()) return;

    this.error.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    // ✅ Sanear: null → undefined
    const valor = this.form.getRawValue();
    const request: AntropometricoRequest = {
      estaturaCm: valor.estaturaCm,
      pesoKg: valor.pesoKg,
      perimetroAbdominalCm: valor.perimetroAbdominalCm ?? undefined,
      porcentajeGrasa: valor.porcentajeGrasa ?? undefined,
      porcentajeMasaMuscular: valor.porcentajeMasaMuscular ?? undefined,
      porcentajeGrasaVisceral: valor.porcentajeGrasaVisceral ?? undefined,
    };

    const peticion = this.modoEdicion()
      ? this.antropometricoService.actualizar(this.idEvaluacion, request)
      : this.antropometricoService.registrar(this.idEvaluacion, request);

    this.loading.set(true);
    this.guardando.set(true);
    peticion.subscribe({
      next: () => this.router.navigate(['/nutricional/evaluaciones', this.idEvaluacion]),
      error: (err) => {
        if (err.status === 409) {
          this.error.set(err.error?.message ?? 'Conflicto: la evaluación cambió de estado.');
        } else if (err.status === 404) {
          this.error.set(err.error?.message ?? 'No existe evaluación o antropometría.');
        } else {
          this.error.set('No se pudo guardar la antropometría.');
        }
        this.loading.set(false);
        this.guardando.set(false);
      },
    });
  }

  cancelar(): void {
    this.router.navigate(['/nutricional/evaluaciones', this.idEvaluacion]);
  }

  protected mensajeValidacion(
    campo:
      | 'estaturaCm'
      | 'pesoKg'
      | 'perimetroAbdominalCm'
      | 'porcentajeGrasa'
      | 'porcentajeMasaMuscular'
      | 'porcentajeGrasaVisceral'
  ): string {
    const control = this.form.controls[campo];

    if (!control.touched) return '';

    if (control.hasError('required')) return 'Este campo es obligatorio.';
    if (control.hasError('min')) {
      return `Valor mínimo: ${control.getError('min').min}.`;
    }
    if (control.hasError('max')) {
      return `Valor máximo: ${control.getError('max').max}.`;
    }
    return '';
  }
}
