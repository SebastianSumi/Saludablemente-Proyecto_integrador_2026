import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import {
  TuiButton,
  TuiError,
  TuiIcon,
  TuiInput,
  TuiLabel,
  TuiTextfield,
} from '@taiga-ui/core';
import { TuiBadge, TuiButtonLoading } from '@taiga-ui/kit';
import { AuthService } from '../auth/auth-service';
import { homePorRol } from '../auth/roles';
import { ThemeService } from '../theme/theme-service';

@Component({
  selector: 'app-login',
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
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private readonly fb = inject(FormBuilder);
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly tema = inject(ThemeService);
  protected readonly ingresando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    identificador: ['', [Validators.required, Validators.maxLength(120)]],
    password: ['', [Validators.required, Validators.maxLength(80)]],
  });

  protected esOscuro(): boolean {
    return this.tema.esOscuro();
  }

  protected alternarTema(): void {
    this.tema.alternar();
  }

  protected irAlPanel(): void {
    this.router.navigate([homePorRol(this.auth.obtenerRoles())]);
  }

  protected ingresar(): void {
    if (this.auth.estaAutenticado()) {
      this.irAlPanel();
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.ingresando.set(true);
    this.error.set(null);
    const { identificador, password } = this.form.getRawValue();

    this.auth.login({ identificador: identificador.trim(), password }).subscribe({
      next: (sesion) => {
        this.ingresando.set(false);
        this.router.navigateByUrl(this.destinoSeguro(sesion.roles));
      },
      error: (err: HttpErrorResponse) => {
        this.ingresando.set(false);
        const mensaje: string = err.error?.message ?? '';
        this.error.set(
          err.status === 401 && mensaje
            ? mensaje
            : 'No se pudo iniciar sesión. Verifica tus credenciales e intenta de nuevo.'
        );
      },
    });
  }

  private destinoSeguro(roles: string[]): string {
    const raw = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/';
    if (!raw.startsWith('/') || raw.startsWith('//') || raw === '/login') {
      return homePorRol(roles);
    }
    const esAdmin = this.auth.tieneAlgunRol(['Administrador']);
    if (!esAdmin && raw.startsWith('/admin')) return homePorRol(roles);
    return raw;
  }
}
