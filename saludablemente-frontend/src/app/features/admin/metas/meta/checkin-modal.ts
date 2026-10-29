import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import {
  TuiButton,
  TuiCheckbox,
  TuiError,
  TuiInput,
  TuiLabel,
  TuiTextfield,
} from '@taiga-ui/core';
import { TuiButtonLoading } from '@taiga-ui/kit';
import { TuiDialogContext } from '@taiga-ui/core/portals/dialog';
import { injectContext } from '@taiga-ui/polymorpheus';
import { MetaService } from '../meta-service';
import { ModalidadMeta, ProgresoMeta } from '../models';

export interface CheckinDatos {
  idMeta: number;
  idPersona: number;
  modalidad: ModalidadMeta;
}

@Component({
  selector: 'app-checkin-modal',
  imports: [
    ReactiveFormsModule,
    TuiButton,
    TuiButtonLoading,
    TuiCheckbox,
    TuiError,
    TuiInput,
    TuiLabel,
    TuiTextfield,
  ],
  templateUrl: './checkin-modal.html',
})
export class CheckinModal {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(MetaService);
  private readonly context =
    injectContext<TuiDialogContext<ProgresoMeta | null, CheckinDatos>>();

  protected readonly esCuantitativa = this.context.data.modalidad === 'CUANTITATIVA';
  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    valorReportado: [null as number | null],
    logrado: [false],
    nota: ['', [Validators.maxLength(300)]],
  });

  protected cancelar(): void {
    this.context.completeWith(null);
  }

  protected guardar(): void {
    const valor = this.form.getRawValue();
    if (this.esCuantitativa && (valor.valorReportado === null || valor.valorReportado < 0)) {
      this.error.set('Ingresa un valor válido mayor o igual a 0.');
      return;
    }
    this.guardando.set(true);
    this.error.set(null);
    this.service
      .registrarCheckIn({
        idMeta: this.context.data.idMeta,
        idPersona: this.context.data.idPersona,
        valorReportado: this.esCuantitativa ? (valor.valorReportado ?? undefined) : undefined,
        logrado: this.esCuantitativa ? undefined : valor.logrado,
        nota: valor.nota.trim() || undefined,
      })
      .subscribe({
        next: (p) => this.context.completeWith(p),
        error: (err: HttpErrorResponse) => {
          this.error.set(err.error?.message ?? 'No se pudo registrar el check-in.');
          this.guardando.set(false);
        },
      });
  }
}
