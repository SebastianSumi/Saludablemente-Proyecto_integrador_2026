import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { TuiButton, TuiDialogService, TuiIcon } from '@taiga-ui/core';
import { TuiBadge } from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { PolymorpheusComponent } from '@taiga-ui/polymorpheus';
import { CategoriaNoticiaService } from '../categoria-service';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { Categoria } from '../models';
import { CategoriaModal, CategoriaModalDatos } from './categoria-modal';

@Component({
  selector: 'app-categoria-list',
  imports: [TuiBadge, TuiButton, TuiIcon, TuiTable],
  templateUrl: './categoria-list.html',
  styleUrl: './categoria-list.css',
})
export class CategoriaList implements OnInit {
  private readonly service = inject(CategoriaNoticiaService);
  private readonly confirmar = inject(ConfirmService);
  private readonly dialogs = inject(TuiDialogService);

  protected readonly items = signal<Categoria[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.listarTodas().subscribe({
      next: (data) => {
        this.items.set(data);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 0 ? 'No se pudo conectar con el servidor.' : 'No se pudo cargar las categorías.'
        );
        this.loading.set(false);
      },
    });
  }

  abrirModal(datos?: CategoriaModalDatos): void {
    this.dialogs
      .open<Categoria | null>(new PolymorpheusComponent(CategoriaModal), {
        label: datos?.id ? 'Editar categoría' : 'Nueva categoría',
        data: datos ?? {},
      })
      .subscribe({
        next: (guardada) => {
          if (guardada) this.cargar();
        },
        complete: () => this.cargar(),
      });
  }

  cambiarEstado(item: Categoria): void {
    const activar = !item.activo;
    this.confirmar
      .confirmar(
        activar ? `¿Activar la categoría "${item.nombre}"?` : `¿Inactivar la categoría "${item.nombre}"?`,
        activar ? 'Activar' : 'Inactivar'
      )
      .subscribe((ok) => {
        if (!ok) return;
        (activar ? this.service.activar(item.id) : this.service.inactivar(item.id)).subscribe({
          next: () => this.cargar(),
          error: () => this.error.set('No se pudo cambiar el estado.'),
        });
      });
  }

  eliminar(item: Categoria): void {
    if (item.esSistema) {
      this.error.set('No se puede eliminar una categoría del sistema.');
      return;
    }
    this.confirmar
      .confirmar(`¿Eliminar la categoría "${item.nombre}"?`, 'Eliminar')
      .subscribe((ok) => {
        if (!ok) return;
        this.service.eliminar(item.id).subscribe({
          next: () => this.cargar(),
          error: (err: HttpErrorResponse) => {
            this.error.set(err.error?.message ?? 'No se pudo eliminar (puede tener noticias).');
          },
        });
      });
  }
}
