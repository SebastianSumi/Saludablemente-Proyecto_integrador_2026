import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  TuiButton,
  TuiCheckbox,
  TuiDropdown,
  TuiError,
  TuiFilterByInputPipe,
  TuiIcon,
  TuiInput,
  TuiLabel,
  TuiTextfield,
  tuiItemsHandlersProvider,
} from '@taiga-ui/core';
import { TuiButtonLoading, TuiChevron, TuiComboBox, TuiDataListWrapper } from '@taiga-ui/kit';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { UsuarioService } from '../usuario-service';
import { RolService } from '../rol-service';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { PersonaService } from '../../personal/persona/persona-service';
import { Rol } from '../models';
import { Persona } from '../../personal/persona/persona.model';

@Component({
  selector: 'app-usuario-form',
  imports: [
    FormsModule,
    ReactiveFormsModule,
    TuiButton,
    TuiButtonLoading,
    TuiCheckbox,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown,
    TuiError,
    TuiFilterByInputPipe,
    TuiIcon,
    TuiInput,
    TuiLabel,
    TuiTextfield,
  ],
  templateUrl: './usuario-form.html',
  styleUrl: './usuario-form.css',
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal(
        (item: unknown): string => {
          const o = item as { nombre?: string } | null;
          return o?.nombre ?? (typeof item === 'string' ? (item as string) : '');
        }
      ),
      identityMatcher: signal(
        (a: unknown, b: unknown): boolean =>
          ((a as { id?: unknown } | null)?.id ?? a ?? null) ===
          ((b as { id?: unknown } | null)?.id ?? b ?? null)
      ),
    }),
  ],
})
export class UsuarioForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(UsuarioService);
  private readonly rolesSrv = inject(RolService);
  private readonly personasSrv = inject(PersonaService);
  private readonly confirmar = inject(ConfirmService);
  private readonly router = inject(Router);

  protected readonly personas = signal<Persona[]>([]);
  protected readonly roles = signal<Rol[]>([]);
  protected readonly rolesElegidos = signal<number[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly guardando = signal(false);

  protected readonly form = this.fb.nonNullable.group(
    {
      email: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
      password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(80)]],
      confirmarPassword: ['', [Validators.required]],
      persona: [null as Persona | null, [Validators.required]],
    },
    {
      validators: (g) => {
        const f = g as FormGroup;
        return f.controls['password'].value === f.controls['confirmarPassword'].value
          ? null
          : { noCoincide: true };
      },
    }
  );

  ngOnInit(): void {
    this.loading.set(true);
    forkJoin({
      pers: this.personasSrv.listar({ estado: true }).pipe(catchError(() => of([]))),
      roles: this.rolesSrv.listar().pipe(catchError(() => of([]))),
    }).subscribe(({ pers, roles }) => {
      this.personas.set(pers);
      this.roles.set(roles);
      this.loading.set(false);
    });
  }

  protected nombrePersona(p: Persona): string {
    return [p.apellidoPaterno, p.apellidoMaterno, p.nombres].filter((x) => !!x).join(' ');
  }

  protected toggleRol(id: number, marcado: boolean): void {
    this.rolesElegidos.update((ids) =>
      marcado ? [...new Set([...ids, id])] : ids.filter((x) => x !== id)
    );
  }

  guardar(): void {
    if (this.loading() || this.guardando()) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Completa email, contraseña válida (8+) y persona.');
      return;
    }
    if (this.form.hasError('noCoincide')) {
      this.error.set('Las contraseñas no coinciden.');
      return;
    }
    if (this.rolesElegidos().length === 0) {
      this.error.set('Selecciona al menos un rol.');
      return;
    }
    const v = this.form.getRawValue();
    const persona = v.persona as Persona;
    const payload = {
      email: (v.email ?? '').trim(),
      password: v.password ?? '',
      idPersona: persona.id,
      rolesIds: this.rolesElegidos(),
    };
    this.confirmar
      .confirmar(`¿Crear la cuenta ${payload.email} vinculada a ${this.nombrePersona(persona)}?`, 'Crear')
      .subscribe((ok) => {
        if (!ok) return;
        this.guardando.set(true);
        this.error.set(null);
        this.service.crear(payload).subscribe({
          next: (u) => this.router.navigate(['/seguridad/usuarios', u.id]),
          error: (err: HttpErrorResponse) => {
            if (err.status === 409) {
              this.error.set(err.error?.message ?? 'Email duplicado o persona con cuenta.');
            } else if (err.status === 400) {
              this.error.set(err.error?.message ?? 'Datos inválidos.');
            } else {
              this.error.set('No se pudo crear el usuario.');
            }
            this.guardando.set(false);
          },
        });
      });
  }

  cancelar(): void {
    this.router.navigate(['/seguridad/usuarios']);
  }
}
