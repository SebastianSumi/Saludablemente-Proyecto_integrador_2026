import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import {
  TuiButton,
  TuiDropdown, TuiFilterByInputPipe,
  TuiError,
  TuiInput,
  TuiLabel,
  TuiTextfield,
} from '@taiga-ui/core';
import { TuiButtonLoading, TuiChevron, TuiDataListWrapper, TuiComboBox } from '@taiga-ui/kit';
import { TuiDialogContext } from '@taiga-ui/core/portals/dialog';
import { injectContext } from '@taiga-ui/polymorpheus';
import { ExoneracionService } from '../exoneracion-service';
import { PerfilService } from '../../perfil/perfil-service';
import { Justificacion } from '../models';

export interface DictamenDatos {
  idJustificacion: number;
}

@Component({
  selector: 'app-dictamen-modal',
  imports: [
    ReactiveFormsModule,
    TuiButton,
    TuiButtonLoading,
    TuiChevron,
    TuiDataListWrapper,
    TuiDropdown, TuiFilterByInputPipe,
    TuiError,
    TuiInput,
    TuiLabel,
    TuiComboBox,
    TuiTextfield,
  ],
  templateUrl: './dictamen-modal.html',
})
export class DictamenModal {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ExoneracionService);
  private readonly perfil = inject(PerfilService);
  private readonly context =
    injectContext<TuiDialogContext<Justificacion | null, DictamenDatos>>();

  protected readonly estados: string[] = ['APROBADO', 'RECHAZADO'];
  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected idUsuario = 0;

  protected readonly form = this.fb.nonNullable.group({
    estado: ['APROBADO', [Validators.required]],
    observaciones: ['', [Validators.maxLength(300)]],
  });

  constructor() {
    this.perfil.obtener().subscribe({
      next: (p) => {
        this.idUsuario = p.idUsuario;
      },
      error: () => undefined,
    });
  }

  protected cancelar(): void {
    this.context.completeWith(null);
  }

  protected dictaminar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const valor = this.form.getRawValue();
    this.guardando.set(true);
    this.error.set(null);
    this.service
      .dictaminar(this.context.data.idJustificacion, {
        estado: valor.estado as 'APROBADO' | 'RECHAZADO',
        observaciones: valor.observaciones.trim() || undefined,
        idUsuarioDictamina: this.idUsuario,
      })
      .subscribe({
        next: (j) => this.context.completeWith(j),
        error: (err: HttpErrorResponse) => {
          this.error.set(err.error?.message ?? 'No se pudo dictaminar.');
          this.guardando.set(false);
        },
      });
  }
}
