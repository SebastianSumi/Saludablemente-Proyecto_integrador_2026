import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TuiButton, TuiError, TuiIcon, TuiInput, TuiLabel, TuiTextfield } from '@taiga-ui/core';
import { TuiButtonLoading, TuiTextarea } from '@taiga-ui/kit';
import { TeamService } from './team-service';
import { switchMap, of } from 'rxjs';
import { AuthService } from '../../../core/auth/auth-service';

@Component({
  selector: 'app-team-form',
  imports: [ReactiveFormsModule, TuiButton, TuiButtonLoading, TuiError, TuiIcon, TuiInput, TuiLabel, TuiTextfield, TuiTextarea],
  templateUrl: './team-form.html',
  styleUrl: './team-form.css',
})
export class TeamForm {
  private readonly fb = inject(FormBuilder);
  private readonly teamService = inject(TeamService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  protected readonly esAdministrador = inject(AuthService).tieneRol('Administrador');
  protected readonly candidatos = signal<{ idUsuario: number; nombre: string }[]>([]);
  protected readonly liderActual = signal<number | null>(null);
  protected readonly guardandoLider = signal(false);
  protected readonly mensajeLider = signal<string | null>(null);
  protected readonly puedeEditar = signal(this.esAdministrador);

  protected readonly id = signal<number | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly errorCarga = signal(false);
  protected readonly guardando = signal(false);
  protected readonly imagenUrl = signal<string | null>(null);
  private archivo: File | null = null;
  private creadoId: number | null = null;

  protected readonly form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    descripcion: ['', [Validators.maxLength(255)]],
    color: ['#2563EB', [Validators.pattern(/^#[0-9a-fA-F]{6}$/)]],
    slogan: ['', [Validators.maxLength(150)]],
    referenciaBiblica: ['', [Validators.maxLength(100)]],
    textoVersiculo: ['', [Validators.maxLength(1000)]],
  });

  constructor() {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.id.set(id);
      this.puedeEditar.set(false);
      this.teamService.puedeEditar(id).subscribe({
        next: permitido => { this.puedeEditar.set(permitido); if (!permitido) this.error.set('No tienes permiso para editar este equipo.'); },
        error: () => this.error.set('No se pudieron verificar tus permisos.'),
      });
      this.loading.set(true);
      this.teamService.obtener(id).subscribe({
        next: (equipo) => {
          this.form.patchValue({
            nombre: equipo.nombre,
            descripcion: equipo.descripcion ?? '',
            color: equipo.color ?? '#2563EB',
            slogan: equipo.slogan ?? '',
            referenciaBiblica: equipo.referenciaBiblica ?? '',
            textoVersiculo: equipo.textoVersiculo ?? '',
          });
          this.imagenUrl.set(equipo.imagenUrl ?? null);
          this.liderActual.set(equipo.idUsuarioLider ?? null);
          if (this.esAdministrador) this.teamService.candidatos(id).subscribe({
            next: candidatos => this.candidatos.set(candidatos),
            error: () => this.error.set('No se pudieron cargar los candidatos a líder.'),
          });
          this.loading.set(false);
        },
        error: () => {
          this.errorCarga.set(true);
          this.error.set('No se pudo cargar el equipo.');
          this.loading.set(false);
        },
      });
    }
  }

  guardar(): void {
    if (!this.puedeEditar() || this.loading() || this.errorCarga() || this.guardando()) return;

    this.error.set(null);

    const nombre = this.form.controls.nombre;
    nombre.setValue(nombre.value.trim());

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valor = this.form.getRawValue();
    const id = this.id() ?? this.creadoId;
    const peticion = id
      ? this.teamService.actualizar(id, valor)
      : this.teamService.crear(valor);

    this.guardando.set(true);
    peticion.pipe(switchMap(equipo => {
      this.creadoId = equipo.id;
      return this.archivo ? this.teamService.subirImagen(equipo.id, this.archivo) : of(equipo);
    })).subscribe({
      next: () => this.router.navigate([this.esAdministrador ? '/teams' : '/mi-equipo']),
      error: (err: HttpErrorResponse) => {
        if (err.status === 409) {
          this.error.set(err.error?.message ?? 'Ya existe un equipo con ese nombre.');
        } else if (err.status === 400) {
          this.error.set(err.error?.message ?? 'Datos inválidos.');
        } else {
          this.error.set(err.error?.message ?? 'No se pudo completar el guardado. Puedes reintentar sin duplicar el equipo.');
        }
        this.guardando.set(false);
      },
    });
  }

  cancelar(): void {
    this.router.navigate([this.esAdministrador ? '/teams' : '/mi-equipo']);
  }

  asignarLider(valor: string): void {
    const id = this.id();
    if (!id || this.guardandoLider()) return;
    this.guardandoLider.set(true);
    this.mensajeLider.set(null);
    this.teamService.asignarLider(id, valor ? Number(valor) : null).subscribe({
      next: equipo => {
        this.liderActual.set(equipo.idUsuarioLider ?? null);
        this.guardandoLider.set(false);
        this.mensajeLider.set('Liderazgo actualizado.');
      },
      error: (err: HttpErrorResponse) => {
        this.guardandoLider.set(false);
        this.error.set(err.error?.message ?? 'No se pudo asignar el líder.');
      },
    });
  }

  seleccionarImagen(event: Event): void {
    const archivo = (event.target as HTMLInputElement).files?.[0];
    if (!archivo) return;
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(archivo.type) || archivo.size > 5 * 1024 * 1024) {
      this.error.set('Selecciona JPEG, PNG o WebP de hasta 5 MB.');
      return;
    }
    this.archivo = archivo;
    const lector = new FileReader();
    lector.onload = () => this.imagenUrl.set(lector.result as string);
    lector.readAsDataURL(archivo);
    this.error.set(null);
  }

  protected mensajeValidacion(campo: 'nombre' | 'descripcion'): string {
    const control = this.form.controls[campo];
    if (!control.touched) return '';

    if (control.hasError('required')) {
      return 'Este campo es obligatorio.';
    }
    if (control.hasError('maxlength')) {
      return `Máximo ${control.getError('maxlength').requiredLength} caracteres.`;
    }
    return '';
  }
}
