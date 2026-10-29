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
import { CuarentenaService } from '../cuarentena-service';
import { PerfilService } from '../../perfil/perfil-service';
import { Cuarentena } from '../models';

export interface ResolverCuarentenaDatos {
  idCuarentena: number;
}

@Component({
  selector: 'app-resolver-cuarentena-modal',
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
  templateUrl: './resolver-cuarentena-modal.html',
})
export class ResolverCuarentenaModal {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(CuarentenaService);
  private readonly perfil = inject(PerfilService);
  private readonly context =
    injectContext<TuiDialogContext<Cuarentena | null, ResolverCuarentenaDatos>>();

  protected readonly decisiones: string[] = ['APROBAR', 'RECHAZAR'];
  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected idUsuario = 0;

  protected readonly form = this.fb.nonNullable.group({
    decision: ['APROBAR', [Validators.required]],
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

  protected resolver(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const valor = this.form.getRawValue();
    this.guardando.set(true);
    this.error.set(null);
    this.service
      .resolver(this.context.data.idCuarentena, {
        decision: valor.decision as 'APROBAR' | 'RECHAZAR',
        observaciones: valor.observaciones.trim() || undefined,
        idUsuarioResuelve: this.idUsuario,
      })
      .subscribe({
        next: (c) => this.context.completeWith(c),
        error: (err: HttpErrorResponse) => {
          this.error.set(err.error?.message ?? 'No se pudo resolver el registro.');
          this.guardando.set(false);
        },
      });
  }
}
