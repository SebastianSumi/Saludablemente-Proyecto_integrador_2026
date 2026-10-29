import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { TuiButton, TuiDialogService, TuiDropdown, TuiFilterByInputPipe, TuiIcon, tuiItemsHandlersProvider } from '@taiga-ui/core';
import {
  TuiAvatar,
  TuiBadge,
  TuiChevron,
  TuiDataListWrapper,
  TuiComboBox,
} from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { PolymorpheusComponent } from '@taiga-ui/polymorpheus';
import { ExoneracionService } from '../exoneracion-service';
import { AusentismoModal } from './ausentismo-modal';
import { DictamenModal } from './dictamen-modal';
import { EstadoJustificacion, Justificacion } from '../models';

interface Opcion {
  id: string;
  label: string;
}

@Component({
  selector: 'app-exoneracion-list',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    TuiAvatar,
    TuiButton,
    TuiBadge,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown, TuiFilterByInputPipe,
    TuiIcon,
    TuiTable,
  ],
  templateUrl: './exoneracion-list.html',
  styleUrl: './exoneracion-list.css',
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal(
        (item: unknown): string => (item as { label?: string } | null)?.label ?? ''
      ),
      identityMatcher: signal(
        (a: unknown, b: unknown): boolean =>
          ((a as { id?: unknown } | null)?.id ?? null) ===
          ((b as { id?: unknown } | null)?.id ?? null)
      ),
    }),
  ],
})
export class ExoneracionList implements OnInit {
  private readonly service = inject(ExoneracionService);
  private readonly dialogs = inject(TuiDialogService);

  protected readonly items = signal<Justificacion[]>([]);
  protected readonly total = signal(0);
  protected readonly totalPages = signal(1);
  protected readonly page = signal(0);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly estadoOpts: Opcion[] = [
    { id: '', label: 'Todos' },
    { id: 'PENDIENTE', label: 'Pendiente' },
    { id: 'APROBADO', label: 'Aprobado' },
    { id: 'RECHAZADO', label: 'Rechazado' },
  ];

  protected readonly estadoCtrl = new FormControl<Opcion | null>(null);

  ngOnInit(): void {
    this.estadoCtrl.valueChanges.subscribe(() => {
      this.page.set(0);
      this.cargar();
    });
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service
      .listar({
        estado: (this.estadoCtrl.value?.id || undefined) as EstadoJustificacion | undefined,
        page: this.page(),
        size: 20,
      })
      .subscribe({
        next: (p) => {
          this.items.set(p.content);
          this.total.set(p.totalElements);
          this.totalPages.set(Math.max(1, p.totalPages));
          this.loading.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.error.set(
            err.status === 0 ? 'No se pudo conectar con el servidor.' : 'No se pudo cargar las solicitudes.'
          );
          this.loading.set(false);
        },
      });
  }

  paginaAnterior(): void {
    if (this.page() > 0) {
      this.page.update((v) => v - 1);
      this.cargar();
    }
  }

  paginaSiguiente(): void {
    if (this.page() + 1 < this.totalPages()) {
      this.page.update((v) => v + 1);
      this.cargar();
    }
  }

  dictaminar(item: Justificacion): void {
    this.dialogs
      .open<Justificacion | null>(new PolymorpheusComponent(DictamenModal), {
        label: `Dictaminar solicitud #${item.idJustificacion}`,
        data: { idJustificacion: item.idJustificacion },
      })
      .subscribe({
        next: (res) => {
          if (res) this.cargar();
        },
        complete: () => this.cargar(),
      });
  }

  verAusentismo(): void {
    this.dialogs
      .open(new PolymorpheusComponent(AusentismoModal), {
        label: 'Ausentismo crónico',
        data: undefined,
      })
      .subscribe();
  }

  protected aparienciaEstado(e: EstadoJustificacion): string {
    switch (e) {
      case 'PENDIENTE':
        return 'warning';
      case 'APROBADO':
        return 'positive';
      default:
        return 'negative';
    }
  }
}