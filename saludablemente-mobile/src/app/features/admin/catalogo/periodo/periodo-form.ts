import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TuiButton, TuiCalendar, TuiError, TuiIcon, TuiInput, TuiLabel, TuiTextfield } from '@taiga-ui/core';
import { TuiButtonLoading, TuiInputDate } from '@taiga-ui/kit';
import { TuiDay } from '@taiga-ui/cdk/date-time';
import { PeriodoService } from './periodo-service';

@Component({
  selector: 'app-periodo-form',
  imports: [ReactiveFormsModule, TuiButton, TuiButtonLoading, TuiCalendar, TuiError, TuiIcon, TuiInput, TuiInputDate, TuiLabel, TuiTextfield],
  templateUrl: './periodo-form.html',
  styleUrl: './periodo-form.css',
})
export class PeriodoForm {
  private readonly fb = inject(FormBuilder);
  private readonly periodoService = inject(PeriodoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly id = signal<number | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly errorCarga = signal(false);
  protected readonly guardando = signal(false);

  protected readonly form = this.fb.nonNullable.group({
    codigo: ['', [
      Validators.required,
      Validators.pattern(/^\d{4}-[12]$/),
    ]],
    descripcion: ['', [
      Validators.required,
      Validators.maxLength(100),
    ]],
    fechaInicio: ['', [Validators.required]],
    fechaFin: ['', [Validators.required]],
  });

  constructor() {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.id.set(id);
      this.loading.set(true);
      this.periodoService.obtener(id).subscribe({
        next: (periodo) => {
          this.form.patchValue({
            codigo: periodo.codigo,
            descripcion: periodo.descripcion,
            fechaInicio: periodo.fechaInicio,
            fechaFin: periodo.fechaFin,
          });
          this.loading.set(false);
        },
        error: () => {
          this.errorCarga.set(true);
          this.error.set('No se pudo cargar el periodo.');
          this.loading.set(false);
        },
      });
    }
  }

  guardar(): void {
    if (this.loading() || this.errorCarga() || this.guardando()) return;

    this.error.set(null);

    const codigo = this.form.controls.codigo;
    codigo.setValue(codigo.value.trim());

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valor = this.form.getRawValue();

    if (valor.fechaInicio >= valor.fechaFin) {
      this.error.set('La fecha de inicio debe ser anterior a la fecha de fin.');
      return;
    }

    const id = this.id();
    const peticion = id
      ? this.periodoService.actualizar(id, valor)
      : this.periodoService.crear(valor);

    this.guardando.set(true);
    peticion.subscribe({
      next: () => this.router.navigate(['/catalogo/periodos']),
      error: (err) => {
        if (err.status === 409) {
          this.error.set(err.error?.message ?? 'Ya existe un periodo con ese código.');
        } else {
          this.error.set('No se pudo guardar el periodo.');
        }
        this.guardando.set(false);
      },
    });
  }

  cancelar(): void {
    this.router.navigate(['/catalogo/periodos']);
  }

  protected fijarHoy(campo: 'fechaInicio' | 'fechaFin'): void {
    const control = this.form.controls[campo];
    control.setValue(TuiDay.currentLocal().toJSON());
    control.markAsTouched();
  }

  protected limpiarCampo(campo: 'fechaInicio' | 'fechaFin'): void {
    const control = this.form.controls[campo];
    control.setValue('');
    control.markAsTouched();
  }

  protected mensajeValidacion(campo: 'codigo' | 'descripcion' | 'fechaInicio' | 'fechaFin'): string {
    const control = this.form.controls[campo];

    if (!control.touched) return '';

    if (control.hasError('required')) {
      return 'Este campo es obligatorio.';
    }
    if (control.hasError('pattern')) {
      return 'El código debe tener el formato YYYY-1 o YYYY-2 (ej. 2026-1).';
    }
    if (control.hasError('maxlength')) {
      return `Máximo ${control.getError('maxlength').requiredLength} caracteres.`;
    }
    return '';
  }
}
