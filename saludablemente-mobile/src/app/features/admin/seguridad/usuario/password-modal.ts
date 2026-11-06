import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { TuiButton, TuiError, TuiIcon, TuiInput, TuiLabel, TuiTextfield } from '@taiga-ui/core';
import { TuiButtonLoading } from '@taiga-ui/kit';
import { TuiDialogContext } from '@taiga-ui/core/portals/dialog';
import { injectContext } from '@taiga-ui/polymorpheus';
import { UsuarioService } from '../usuario-service';

export interface PasswordModalDatos {
  idUsuario: number;
  email: string;
}

@Component({
  selector: 'app-password-modal',
  imports: [
    ReactiveFormsModule,
    TuiButton,
    TuiButtonLoading,
    TuiError,
    TuiIcon,
    TuiInput,
    TuiLabel,
    TuiTextfield,
  ],
  templateUrl: './password-modal.html',
})
export class PasswordModal {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(UsuarioService);
  private readonly context =
    injectContext<TuiDialogContext<boolean, PasswordModalDatos>>();

  protected readonly email = this.context.data.email;
  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group(
    {
      nueva: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(80)]],
      confirmar: ['', [Validators.required]],
    },
    {
      validators: (g) => {
        const f = g as FormGroup;
        return f.controls['nueva'].value === f.controls['confirmar'].value
          ? null
          : { noCoincide: true };
      },
    }
  );

  protected cancelar(): void {
    this.context.completeWith(false);
  }

  protected guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    if (this.form.hasError('noCoincide')) {
      this.error.set('Las contraseñas no coinciden.');
      return;
    }
    this.guardando.set(true);
    this.error.set(null);
    this.service
      .actualizarPassword(this.context.data.idUsuario, {
        newPassword: this.form.controls.nueva.value,
      })
      .subscribe({
        next: () => this.context.completeWith(true),
        error: (err: HttpErrorResponse) => {
          this.error.set(err.error?.message ?? 'No se pudo cambiar la contraseña.');
          this.guardando.set(false);
        },
      });
  }
}
