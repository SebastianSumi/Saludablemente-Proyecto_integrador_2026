import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { TuiButton, TuiIcon, TuiInput, TuiLabel, TuiTextfield } from '@taiga-ui/core';
import { TuiButtonLoading } from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { TuiDialogContext } from '@taiga-ui/core/portals/dialog';
import { injectContext } from '@taiga-ui/polymorpheus';
import { ExoneracionService } from '../exoneracion-service';
import { Ausentismo } from '../models';

@Component({
  selector: 'app-ausentismo-modal',
  imports: [ReactiveFormsModule, TuiButton, TuiButtonLoading, TuiIcon, TuiInput, TuiLabel, TuiTextfield, TuiTable],
  templateUrl: './ausentismo-modal.html',
})
export class AusentismoModal implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ExoneracionService);
  private readonly context = injectContext<TuiDialogContext<null, void>>();

  protected readonly datos = signal<Ausentismo | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly form = this.fb.nonNullable.group({
    minRacha: [3],
  });

  ngOnInit(): void {
    this.consultar();
  }

  protected consultar(): void {
    const minRacha = Number(this.form.controls.minRacha.value) || 3;
    this.loading.set(true);
    this.error.set(null);
    this.service.detectarAusentismo(minRacha).subscribe({
      next: (r) => {
        this.datos.set(r);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo consultar el ausentismo.');
        this.loading.set(false);
      },
    });
  }

  protected cerrar(): void {
    this.context.completeWith(null);
  }
}
