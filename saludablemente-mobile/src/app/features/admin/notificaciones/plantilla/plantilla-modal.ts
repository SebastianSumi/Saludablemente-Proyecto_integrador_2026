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
import { TuiButtonLoading, TuiTextarea } from '@taiga-ui/kit';
import { TuiDialogContext } from '@taiga-ui/core/portals/dialog';
import { injectContext } from '@taiga-ui/polymorpheus';
import { PlantillaService } from '../notificacion-service';
import { CanalNotificacion, Plantilla } from '../models';

export interface PlantillaModalDatos {
  id?: number;
  codigoEvento?: string;
  canal?: string;
  asunto?: string;
  cuerpoHtml?: string;
  activo?: boolean;
}

@Component({
  selector: 'app-plantilla-modal',
  imports: [
    ReactiveFormsModule,
    TuiButton,
    TuiButtonLoading,
    TuiCheckbox,
    TuiError,
    TuiInput,
    TuiLabel,
    TuiTextfield,
    TuiTextarea,
  ],
  templateUrl: './plantilla-modal.html',
})
export class PlantillaModal {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(PlantillaService);
  private readonly context =
    injectContext<TuiDialogContext<Plantilla | null, PlantillaModalDatos>>();

  protected readonly canales: CanalNotificacion[] = ['EMAIL', 'IN_APP', 'PUSH', 'SMS'];
  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    codigoEvento: [this.context.data?.codigoEvento ?? '', [Validators.required, Validators.maxLength(60)]],
    canal: [((this.context.data?.canal as CanalNotificacion) ?? 'IN_APP') as CanalNotificacion, [Validators.required]],
    asunto: [this.context.data?.asunto ?? '', [Validators.maxLength(200)]],
    cuerpoHtml: [this.context.data?.cuerpoHtml ?? '', [Validators.required]],
    activo: [this.context.data?.activo ?? true],
  });

  protected cancelar(): void {
    this.context.completeWith(null);
  }

  protected guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const payload = {
      codigoEvento: (v.codigoEvento ?? '').trim(),
      canal: v.canal,
      asunto: (v.asunto ?? '').trim() || undefined,
      cuerpoHtml: v.cuerpoHtml ?? '',
      activo: v.activo ?? true,
    };
    const id = this.context.data?.id;
    this.guardando.set(true);
    this.error.set(null);
    (id ? this.service.actualizar(id, payload) : this.service.crear(payload)).subscribe({
      next: (p) => this.context.completeWith(p),
      error: (err: HttpErrorResponse) => {
        if (err.status === 409) {
          this.error.set(
            `Ya existe una plantilla con el código "${payload.codigoEvento}" en el canal ${payload.canal}. Usa otro código o edita la existente.`,
          );
        } else if (err.status === 400) {
          this.error.set(err.error?.message ?? 'Revisa los campos: hay errores de validación.');
        } else {
          this.error.set(err.error?.message ?? 'No se pudo guardar la plantilla.');
        }
        this.guardando.set(false);
      },
    });
  }
}
