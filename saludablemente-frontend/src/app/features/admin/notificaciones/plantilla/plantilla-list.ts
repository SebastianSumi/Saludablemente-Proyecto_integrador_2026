import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { TuiButton, TuiDialogService, TuiIcon } from '@taiga-ui/core';
import { TuiBadge } from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { PolymorpheusComponent } from '@taiga-ui/polymorpheus';
import { PlantillaService } from '../notificacion-service';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { Plantilla, PlantillaRequest } from '../models';
import { PlantillaModal, PlantillaModalDatos } from './plantilla-modal';

@Component({
  selector: 'app-plantilla-list',
  imports: [FormsModule, TuiBadge, TuiButton, TuiIcon, TuiTable],
  templateUrl: './plantilla-list.html',
  styleUrl: './plantilla-list.css',
})
export class PlantillaList implements OnInit {
  private readonly service = inject(PlantillaService);
  private readonly confirmar = inject(ConfirmService);
  private readonly dialogs = inject(TuiDialogService);

  protected readonly items = signal<Plantilla[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly canales = ['EMAIL', 'IN_APP', 'PUSH', 'SMS'];
  protected readonly filtroTexto = signal('');
  protected readonly filtroCanal = signal('');
  protected readonly filtroActivo = signal('');

  protected readonly filtradas = computed(() => {
    const q = this.filtroTexto().trim().toLowerCase();
    const canal = this.filtroCanal();
    const activo = this.filtroActivo();
    return this.items().filter((p) => {
      if (q && !(p.codigoEvento ?? '').toLowerCase().includes(q)) return false;
      if (canal && p.canal !== canal) return false;
      if (activo === 'activas' && !p.activo) return false;
      if (activo === 'inactivas' && p.activo) return false;
      return true;
    });
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.listar().subscribe({
      next: (data) => {
        this.items.set(data ?? []);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 0 ? 'No se pudo conectar con el servidor.' : 'No se pudo cargar las plantillas.',
        );
        this.loading.set(false);
      },
    });
  }

  abrirModal(datos?: PlantillaModalDatos): void {
    this.dialogs
      .open<Plantilla | null>(new PolymorpheusComponent(PlantillaModal), {
        label: datos?.id ? 'Editar plantilla' : 'Nueva plantilla',
        data: datos ?? {},
      })
      .subscribe({
        next: (guardada) => {
          if (guardada) this.cargar();
        },
        complete: () => this.cargar(),
      });
  }

  eliminar(item: Plantilla): void {
    this.confirmar
      .confirmar(
        `¿Eliminar la plantilla "${item.codigoEvento}"? ` +
          `Si algún módulo notifica con ese código, sus notificaciones fallarán ` +
          `hasta que se cree una nueva. Para conservarla, mejor desactívala.`,
        'Eliminar',
      )
      .subscribe((ok) => {
        if (!ok) return;
        this.service.eliminar(item.idPlantilla).subscribe({
          next: () => this.cargar(),
          error: (err: HttpErrorResponse) => {
            this.error.set(err.error?.message ?? 'No se pudo eliminar.');
          },
        });
      });
  }

  cambiarEstado(item: Plantilla): void {
    const accion = item.activo ? 'desactivar' : 'activar';
    this.confirmar
      .confirmar(`¿${accion === 'desactivar' ? 'Desactivar' : 'Activar'} la plantilla "${item.codigoEvento}"?`, 'Confirmar')
      .subscribe((ok) => {
        if (!ok) return;
        this.service.obtener(item.idPlantilla).subscribe({
          next: (p) => {
            this.service
              .actualizar(item.idPlantilla, {
                codigoEvento: p.codigoEvento,
                canal: (p.canal ?? 'IN_APP') as PlantillaRequest['canal'],
                asunto: p.asunto ?? undefined,
                cuerpoHtml: p.cuerpoHtml ?? '',
                activo: !item.activo,
              })
              .subscribe({
                next: () => this.cargar(),
                error: (err: HttpErrorResponse) => {
                  this.error.set(err.error?.message ?? 'No se pudo actualizar.');
                },
              });
          },
          error: () => this.error.set('No se pudo leer la plantilla.'),
        });
      });
  }

  limpiarFiltros(): void {
    this.filtroTexto.set('');
    this.filtroCanal.set('');
    this.filtroActivo.set('');
  }
}
