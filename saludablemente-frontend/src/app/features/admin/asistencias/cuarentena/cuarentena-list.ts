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
import { CuarentenaService } from '../cuarentena-service';
import { Cuarentena, EstadoCuarentena, MotivoCuarentena } from '../models';
import { ResolverCuarentenaModal } from './resolver-cuarentena-modal';

interface Opcion {
  id: string;
  label: string;
}

@Component({
  selector: 'app-cuarentena-list',
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
  templateUrl: './cuarentena-list.html',
  styleUrl: './cuarentena-list.css',
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
export class CuarentenaList implements OnInit {
  private readonly service = inject(CuarentenaService);
  private readonly dialogs = inject(TuiDialogService);

  protected readonly items = signal<Cuarentena[]>([]);
  protected readonly total = signal(0);
  protected readonly totalPages = signal(1);
  protected readonly pendientes = signal(0);
  protected readonly page = signal(0);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly estadoOpts: Opcion[] = [
    { id: '', label: 'Todos' },
    { id: 'PENDIENTE', label: 'Pendiente' },
    { id: 'APROBADO', label: 'Aprobado' },
    { id: 'RECHAZADO', label: 'Rechazado' },
  ];

  protected readonly motivoOpts: Opcion[] = [
    { id: '', label: 'Todos' },
    { id: 'FUERA_GEOCERCA', label: 'Fuera de geocerca' },
    { id: 'QR_EXPIRADO', label: 'QR expirado' },
    { id: 'SYNC_TARDIA', label: 'Sincronización tardía' },
    { id: 'PERSONA_INACTIVA', label: 'Persona inactiva' },
    { id: 'DUPLICADO', label: 'Duplicado' },
    { id: 'ACTIVIDAD_CERRADA', label: 'Actividad cerrada' },
  ];

  protected readonly estadoCtrl = new FormControl<Opcion | null>(null);
  protected readonly motivoCtrl = new FormControl<Opcion | null>(null);

  ngOnInit(): void {
    this.estadoCtrl.valueChanges.subscribe(() => {
      this.page.set(0);
      this.cargar();
    });
    this.motivoCtrl.valueChanges.subscribe(() => {
      this.page.set(0);
      this.cargar();
    });
    this.cargar();
    this.service.contarPendientes().subscribe({
      next: (r) => this.pendientes.set(r.total),
      error: () => undefined,
    });
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service
      .listar({
        estado: (this.estadoCtrl.value?.id || undefined) as EstadoCuarentena | undefined,
        motivo: (this.motivoCtrl.value?.id || undefined) as MotivoCuarentena | undefined,
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
            err.status === 0 ? 'No se pudo conectar con el servidor.' : 'No se pudo cargar la bandeja.'
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

  resolver(item: Cuarentena): void {
    this.dialogs
      .open<Cuarentena | null>(new PolymorpheusComponent(ResolverCuarentenaModal), {
        label: `Resolver registro #${item.idCuarentena}`,
        data: { idCuarentena: item.idCuarentena },
      })
      .subscribe({
        next: (resuelto) => {
          if (resuelto) {
            this.cargar();
            this.service.contarPendientes().subscribe({
              next: (r) => this.pendientes.set(r.total),
              error: () => undefined,
            });
          }
        },
        complete: () => this.cargar(),
      });
  }

  protected aparienciaEstado(e: EstadoCuarentena): string {
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