import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { TuiButton, TuiError, TuiInput, TuiLabel, TuiTextfield } from '@taiga-ui/core';
import { TuiButtonLoading } from '@taiga-ui/kit';
import { TuiDialogContext } from '@taiga-ui/core/portals/dialog';
import { injectContext } from '@taiga-ui/polymorpheus';
import { CategoriaNoticiaService } from '../categoria-service';
import { Categoria } from '../models';

export interface CategoriaModalDatos {
  id?: number;
  nombre?: string;
  descripcion?: string;
  colorHex?: string;
  icono?: string;
  orden?: number;
}

@Component({
  selector: 'app-categoria-modal',
  imports: [ReactiveFormsModule, TuiButton, TuiButtonLoading, TuiError, TuiInput, TuiLabel, TuiTextfield],
  templateUrl: './categoria-modal.html',
})
export class CategoriaModal {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(CategoriaNoticiaService);
  private readonly context =
    injectContext<TuiDialogContext<Categoria | null, CategoriaModalDatos>>();

  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    nombre: [this.context.data?.nombre ?? '', [Validators.required, Validators.maxLength(60)]],
    descripcion: [this.context.data?.descripcion ?? '', [Validators.maxLength(200)]],
    colorHex: [this.context.data?.colorHex ?? '', [Validators.maxLength(7)]],
    icono: [this.context.data?.icono ?? '', [Validators.maxLength(60)]],
    orden: [this.context.data?.orden ?? null as number | null],
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
      nombre: (v.nombre ?? '').trim(),
      descripcion: (v.descripcion ?? '').trim() || undefined,
      colorHex: (v.colorHex ?? '').trim() || undefined,
      icono: (v.icono ?? '').trim() || undefined,
      orden: v.orden ?? undefined,
    };
    const id = this.context.data?.id;
    this.guardando.set(true);
    this.error.set(null);
    (id ? this.service.actualizar(id, payload) : this.service.crear(payload)).subscribe({
      next: (c) => this.context.completeWith(c),
      error: (err: HttpErrorResponse) => {
        this.error.set(err.error?.message ?? 'No se pudo guardar la categoría.');
        this.guardando.set(false);
      },
    });
  }
}
