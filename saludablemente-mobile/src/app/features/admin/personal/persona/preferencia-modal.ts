import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import {
  TuiButton,
  TuiCheckbox,
  TuiDropdown, TuiFilterByInputPipe,
  TuiError,
  TuiInput,
  TuiLabel,
  TuiTextfield,
} from '@taiga-ui/core';
import { TuiButtonLoading, TuiChevron, TuiDataListWrapper, TuiComboBox } from '@taiga-ui/kit';
import { TuiDialogContext } from '@taiga-ui/core/portals/dialog';
import { injectContext } from '@taiga-ui/polymorpheus';
import { PersonaService } from './persona-service';
import { PreferenciaResponse } from './persona.model';
import { ConfirmService } from '../../../../core/services/confirm-service';

export interface PreferenciaModalDatos {
  personaId: number;
  canalPreferido?: string;
  horarioContactoInicio?: string;
  horarioContactoFin?: string;
  aceptaRecordatorios?: boolean;
}

@Component({
  selector: 'app-preferencia-modal',
  imports: [
    ReactiveFormsModule,
    TuiButton,
    TuiButtonLoading,
    TuiCheckbox,
    TuiChevron,
    TuiDataListWrapper,
    TuiDropdown, TuiFilterByInputPipe,
    TuiError,
    TuiInput,
    TuiLabel,
    TuiComboBox,
    TuiTextfield,
  ],
  templateUrl: './preferencia-modal.html',
})
export class PreferenciaModal {
  private readonly fb = inject(FormBuilder);
  private readonly personas = inject(PersonaService);
  private readonly confirmar = inject(ConfirmService);
  private readonly context =
    injectContext<TuiDialogContext<PreferenciaResponse | null, PreferenciaModalDatos>>();

  protected readonly canales: string[] = ['WHATSAPP', 'EMAIL', 'SMS'];
  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    canalPreferido: [
      this.context.data?.canalPreferido ?? 'WHATSAPP',
      [Validators.required, Validators.maxLength(30)],
    ],
    horarioContactoInicio: [this.context.data?.horarioContactoInicio ?? '09:00'],
    horarioContactoFin: [this.context.data?.horarioContactoFin ?? '18:00'],
    aceptaRecordatorios: [this.context.data?.aceptaRecordatorios ?? true],
  });

  protected cancelar(): void {
    this.context.completeWith(null);
  }

  protected guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.confirmar
      .confirmar('¿Guardar las preferencias de comunicación?', 'Guardar')
      .subscribe((ok) => {
        if (!ok) return;
        const valor = this.form.getRawValue();
        this.guardando.set(true);
        this.error.set(null);
        this.personas
          .upsertPreferencias(this.context.data.personaId, {
            canalPreferido: valor.canalPreferido,
            horarioContactoInicio: valor.horarioContactoInicio || undefined,
            horarioContactoFin: valor.horarioContactoFin || undefined,
            aceptaRecordatorios: valor.aceptaRecordatorios,
          })
          .subscribe({
            next: (p) => this.context.completeWith(p),
            error: (err: HttpErrorResponse) => {
              if (err.status === 409) {
                this.error.set(err.error?.message ?? 'Franja horaria inválida.');
              } else if (err.status === 400) {
                this.error.set(err.error?.message ?? 'Datos inválidos.');
              } else {
                this.error.set('No se pudieron guardar las preferencias.');
              }
              this.guardando.set(false);
            },
          });
      });
  }
}
