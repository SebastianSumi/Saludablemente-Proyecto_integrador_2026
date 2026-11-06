import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TuiButton, TuiError, TuiIcon, TuiInput, TuiLabel, TuiTextfield } from '@taiga-ui/core';
import { TuiButtonLoading, TuiTextarea } from '@taiga-ui/kit';
import { AreaService } from './area-service';

@Component({
  selector: 'app-area-form',
  imports: [ReactiveFormsModule, TuiButton, TuiButtonLoading, TuiError, TuiIcon, TuiLabel, TuiTextfield, TuiInput, TuiTextarea],
  templateUrl: './area-form.html',
  styleUrl: './area-form.css',
})
export class AreaForm {
  private readonly fb = inject(FormBuilder);
  private readonly areaService = inject(AreaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly id = signal<number | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly errorCarga = signal(false);
  protected readonly guardando = signal(false);

  protected readonly form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    descripcion: ['', [Validators.maxLength(255)]],
  });

  constructor() {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.id.set(id);
      this.loading.set(true);
      this.areaService.obtener(id).subscribe({
        next: (area) => {
          this.form.patchValue({
            nombre: area.nombre,
            descripcion: area.descripcion ?? '',
          });
          this.loading.set(false);
        },
        error: () => {
          this.errorCarga.set(true);
          this.error.set('No se pudo cargar el área.');
          this.loading.set(false);
        },
      });
    }
  }

  guardar(): void {
    if (this.loading() || this.errorCarga() || this.guardando()) return;

    this.error.set(null);

    const nombre = this.form.controls.nombre;
    nombre.setValue(nombre.value.trim());

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valor = this.form.getRawValue();
    const id = this.id();
    const peticion = id
      ? this.areaService.actualizar(id, valor)
      : this.areaService.crear(valor);

    this.guardando.set(true);
    peticion.subscribe({
      next: () => this.router.navigate(['/catalogos/areas']),
      error: (err: HttpErrorResponse) => {
        if (err.status === 409) {
          this.error.set(err.error?.message ?? 'Ya existe un área con ese nombre.');
        } else if (err.status === 400) {
          this.error.set(err.error?.message ?? 'Datos inválidos.');
        } else {
          this.error.set('No se pudo guardar el área.');
        }
        this.guardando.set(false);
      },
    });
  }

  cancelar(): void {
    this.router.navigate(['/catalogos/areas']);
  }
}
