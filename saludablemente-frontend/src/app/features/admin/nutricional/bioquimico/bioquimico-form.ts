import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TuiButton, TuiIcon, TuiInput, TuiLabel, TuiTextfield } from '@taiga-ui/core';
import { TuiButtonLoading } from '@taiga-ui/kit';
import { BioquimicoService } from './bioquimico-service';
import { EvaluacionService } from '../evaluacion/evaluacion-service';
import { EvaluacionResponse } from '../evaluacion/evaluacion.model';

@Component({
  selector: 'app-bioquimico-form',
  imports: [ReactiveFormsModule, RouterLink, TuiButton, TuiButtonLoading, TuiIcon, TuiInput, TuiLabel, TuiTextfield],
  templateUrl: './bioquimico-form.html',
  styleUrl: './bioquimico-form.css',
})
export class BioquimicoForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly bioquimicoService = inject(BioquimicoService);
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
    glucosa: [null as number | null, [Validators.min(0), Validators.max(1000)]],
    colesterol: [null as number | null, [Validators.min(0), Validators.max(1000)]],
    trigliceridos: [null as number | null, [Validators.min(0), Validators.max(2000)]],
    presionSistolica: [null as number | null, [Validators.min(50), Validators.max(250)]],
    presionDiastolica: [null as number | null, [Validators.min(30), Validators.max(150)]],
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

        const bio = evaluacion.detalleBioquimico;
        if (bio) {
          this.modoEdicion.set(true);
          this.form.patchValue({
            glucosa: bio.glucosa,
            colesterol: bio.colesterol,
            trigliceridos: bio.trigliceridos,
            presionSistolica: bio.presionSistolica,
            presionDiastolica: bio.presionDiastolica,
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
    if (this.loading() || this.errorCarga()) return;

    this.error.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valor = this.form.getRawValue();

    const peticion = this.modoEdicion()
      ? this.bioquimicoService.actualizarManual(this.idEvaluacion, valor)
      : this.bioquimicoService.registrarManual(this.idEvaluacion, valor);

    this.loading.set(true);
    peticion.subscribe({
      next: () => this.router.navigate(['/nutricional/evaluaciones', this.idEvaluacion]),
      error: (err) => {
        if (err.status === 409) {
          this.error.set(err.error?.message ?? 'Conflicto: la evaluación cambió de estado.');
        } else if (err.status === 404) {
          this.error.set(err.error?.message ?? 'No existe evaluación o bioquímico.');
        } else {
          this.error.set('No se pudo guardar el bioquímico.');
        }
        this.loading.set(false);
      },
    });
  }

  cancelar(): void {
    this.router.navigate(['/nutricional/evaluaciones', this.idEvaluacion]);
  }

  protected mensajeValidacion(
    campo:
      | 'glucosa'
      | 'colesterol'
      | 'trigliceridos'
      | 'presionSistolica'
      | 'presionDiastolica'
  ): string {
    const control = this.form.controls[campo];

    if (!control.touched) return '';

    if (control.hasError('min')) return `Valor mínimo: ${control.getError('min').min}.`;
    if (control.hasError('max')) return `Valor máximo: ${control.getError('max').max}.`;
    return '';
  }
}
