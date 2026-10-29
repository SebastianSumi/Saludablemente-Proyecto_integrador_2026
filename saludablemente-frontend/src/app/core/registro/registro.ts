import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { TuiButton, TuiError, TuiIcon, TuiInput, TuiLabel, TuiTextfield } from '@taiga-ui/core';
import { TuiBadge, TuiButtonLoading } from '@taiga-ui/kit';
import { RegistroService } from './registro-service';
import { ThemeService } from '../theme/theme-service';

type Paso = 1 | 2 | 3;
type EstadoVerificacion = 'ok' | 'no-encontrada' | 'ya-tiene-cuenta' | 'email-en-uso' | 'inactiva' | 'error';

@Component({
  selector: 'app-registro',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    TuiBadge,
    TuiButton,
    TuiButtonLoading,
    TuiError,
    TuiIcon,
    TuiInput,
    TuiLabel,
    TuiTextfield,
  ],
  templateUrl: './registro.html',
  styleUrl: '../login/login.css',
})
export class Registro {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(RegistroService);
  private readonly router = inject(Router);
  // Aplica el tema guardado o el del sistema (claro/oscuro) también fuera del layout.
  private readonly tema = inject(ThemeService);

  protected esOscuro(): boolean {
    return this.tema.esOscuro();
  }

  protected alternarTema(): void {
    this.tema.alternar();
  }

  protected readonly paso = signal<Paso>(1);
  protected readonly procesando = signal(false);
  protected readonly estado = signal<EstadoVerificacion | null>(null);
  protected readonly nombreMostrado = signal('');
  protected readonly error = signal<string | null>(null);

  protected readonly formIdentidad = this.fb.nonNullable.group({
    documentoIdentidad: ['', [Validators.required, Validators.maxLength(20)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
  });

  protected readonly formClave = this.fb.nonNullable.group({
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(80)]],
    confirmar: ['', [Validators.required]],
  });

  protected verificar(): void {
    if (this.formIdentidad.invalid) {
      this.formIdentidad.markAllAsTouched();
      return;
    }
    this.procesando.set(true);
    this.error.set(null);
    const v = this.formIdentidad.getRawValue();
    this.service
      .verificar({ documentoIdentidad: v.documentoIdentidad.trim(), email: v.email.trim() })
      .subscribe({
        next: (r) => {
          this.nombreMostrado.set(r.nombreMostrado);
          this.estado.set('ok');
          this.paso.set(2);
          this.procesando.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.procesando.set(false);
          const msg: string = err.error?.message ?? '';
          if (err.status === 404) {
            this.estado.set('no-encontrada');
          } else if (err.status === 409 && msg.includes('YA_TIENE_CUENTA')) {
            this.estado.set('ya-tiene-cuenta');
          } else if (err.status === 409 && msg.includes('EMAIL_EN_USO')) {
            this.estado.set('email-en-uso');
            this.error.set('Ese email ya está registrado. Usa otro o recupera tu acceso.');
          } else if (err.status === 409) {
            this.estado.set('inactiva');
            this.error.set(msg || 'No es posible crear la cuenta.');
          } else {
            this.estado.set('error');
            this.error.set(err.status === 0 ? 'No se pudo conectar con el servidor.' : msg || 'No se pudo verificar tu identidad.');
          }
        },
      });
  }

  protected registrar(): void {
    if (this.formClave.invalid) {
      this.formClave.markAllAsTouched();
      return;
    }
    const v = this.formClave.getRawValue();
    if (v.password !== v.confirmar) {
      this.error.set('Las contraseñas no coinciden.');
      return;
    }
    this.procesando.set(true);
    this.error.set(null);
    const id = this.formIdentidad.getRawValue();
    this.service
      .registrar({
        email: id.email.trim(),
        password: v.password,
        documentoIdentidad: id.documentoIdentidad.trim(),
      })
      .subscribe({
        next: () => {
          this.procesando.set(false);
          this.paso.set(3);
        },
        error: (err: HttpErrorResponse) => {
          this.procesando.set(false);
          const msg: string = err.error?.message ?? '';
          if (err.status === 409 && msg.includes('YA_TIENE_CUENTA')) {
            this.estado.set('ya-tiene-cuenta');
          } else {
            this.error.set(err.status === 0 ? 'No se pudo conectar con el servidor.' : msg || 'No se pudo crear la cuenta.');
          }
        },
      });
  }

  protected irALogin(): void {
    this.router.navigate(['/login']);
  }
}
