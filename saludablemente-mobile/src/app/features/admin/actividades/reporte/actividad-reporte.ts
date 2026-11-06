import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import {
  TuiButton,
  TuiCalendar,
  TuiError,
  TuiIcon,
  TuiInput,
  TuiLabel,
  TuiTextfield,
} from '@taiga-ui/core';
import { TuiButtonLoading, TuiInputDate } from '@taiga-ui/kit';
import { fijarHoyEn, limpiarFechaEn } from '../../../../core/forms/fecha-transformers';
import { TuiTable } from '@taiga-ui/addon-table';
import { ReporteActividadService } from '../agenda-service';
import { ReportePeriodo } from '../models';

@Component({
  selector: 'app-actividad-reporte',
  imports: [
    ReactiveFormsModule,
    TuiButton,
    TuiButtonLoading,
    TuiCalendar,
    TuiError,
    TuiIcon,
    TuiInput,
    TuiInputDate,
    TuiLabel,
    TuiTable,
    TuiTextfield,
  ],
  templateUrl: './actividad-reporte.html',
  styleUrl: './actividad-reporte.css',
})
export class ActividadReporte {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ReporteActividadService);

  protected readonly reporte = signal<ReportePeriodo | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    fechaInicio: ['', [Validators.required]],
    fechaFin: ['', [Validators.required]],
    periodo: [''],
  });

  consultar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    if (v.fechaInicio > v.fechaFin) {
      this.error.set('La fecha de inicio debe ser anterior a la de fin.');
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.service
      .porPeriodo(v.fechaInicio, v.fechaFin, v.periodo.trim() || undefined)
      .subscribe({
        next: (r) => {
          this.reporte.set(r);
          this.loading.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.error.set(
            err.status === 0 ? 'No se pudo conectar con el servidor.' : 'No se pudo generar el reporte.'
          );
          this.loading.set(false);
        },
      });
  }

  protected fijarHoy(campo: string): void {
    fijarHoyEn(this.form.get(campo));
  }

  protected limpiarCampo(campo: string): void {
    limpiarFechaEn(this.form.get(campo));
  }
}
