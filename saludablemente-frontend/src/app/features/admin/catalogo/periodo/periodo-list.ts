import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { TuiButton, TuiDropdown, TuiFilterByInputPipe, TuiIcon, TuiLabel, TuiTextfield, tuiItemsHandlersProvider } from '@taiga-ui/core';
import { TuiBadge, TuiChevron, TuiComboBox, TuiDataListWrapper } from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { PeriodoService } from './periodo-service';
import { EstadoPeriodo, Periodo } from './periodo.model';
import { ConfirmService } from '../../../../core/services/confirm-service';

interface FiltroOpcion {
  id: string;
  label: string;
}

@Component({
  selector: 'app-periodo-list',
  imports: [RouterLink, ReactiveFormsModule, TuiButton, TuiBadge, TuiChevron, TuiComboBox, TuiDataListWrapper, TuiDropdown, TuiFilterByInputPipe, TuiIcon, TuiLabel, TuiTextfield, TuiTable],
  templateUrl: './periodo-list.html',
  styleUrl: './periodo-list.css',
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
export class PeriodoList implements OnInit {
  private readonly periodoService = inject(PeriodoService);
  private readonly confirmar = inject(ConfirmService);

  protected readonly periodos = signal<Periodo[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly filtroEstado = signal<EstadoPeriodo | ''>('');

  protected readonly columnas = ['codigo', 'descripcion', 'inicio', 'fin', 'estado', 'acciones'];

  protected readonly estadoOpts: FiltroOpcion[] = [
    { id: '', label: 'Todos' },
    { id: 'PLANIFICADO', label: 'Planificado' },
    { id: 'VIGENTE', label: 'Vigente' },
    { id: 'CERRADO', label: 'Cerrado' },
  ];

  protected readonly estadoCtrl = new FormControl<FiltroOpcion | null>(null);

  ngOnInit(): void {
    this.sincronizarControl();
    this.estadoCtrl.valueChanges.subscribe((o) => {
      this.filtroEstado.set((o?.id ?? '') as EstadoPeriodo | '');
      this.cargar();
    });
    this.cargar();
  }

  private sincronizarControl(): void {
    const estado = this.filtroEstado();
    this.estadoCtrl.setValue(
      this.estadoOpts.find((o) => o.id === estado) ?? null,
      { emitEvent: false }
    );
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);

    const estado = this.filtroEstado();
    this.periodoService.listar(estado === '' ? undefined : estado).subscribe({
      next: (data) => this.periodos.set(data),
      error: () => {
        this.error.set('No se pudo cargar la lista de periodos.');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  cambiarFiltro(event: Event): void {
    const valor = (event.target as HTMLSelectElement).value as EstadoPeriodo | '';
    this.filtroEstado.set(valor);
    this.cargar();
  }

  marcarVigente(periodo: Periodo): void {
    if (!periodo.id) return;
    this.confirmar
      .confirmar(`¿Marcar el periodo ${periodo.codigo} como VIGENTE?`, 'Marcar vigente')
      .subscribe((ok) => {
        if (!ok) return;
        this.periodoService.marcarVigente(periodo.id as number).subscribe({
          next: () => this.cargar(),
          error: (err: HttpErrorResponse) => {
            if (err.status === 409) {
              this.error.set(err.error?.message ?? 'Ya existe un periodo VIGENTE.');
            } else {
              this.error.set('No se pudo marcar el periodo como VIGENTE.');
            }
          },
        });
      });
  }

  cerrar(periodo: Periodo): void {
    if (!periodo.id) return;
    this.confirmar
      .confirmar(`¿Cerrar el periodo ${periodo.codigo}? No podrá reabrirse.`, 'Cerrar')
      .subscribe((ok) => {
        if (!ok) return;
        this.periodoService.cerrar(periodo.id as number).subscribe({
          next: () => this.cargar(),
          error: (err: HttpErrorResponse) => {
            if (err.status === 409) {
              this.error.set(err.error?.message ?? 'El periodo ya está cerrado.');
            } else {
              this.error.set('No se pudo cerrar el periodo.');
            }
          },
        });
      });
  }

  eliminar(periodo: Periodo): void {
    if (!periodo.id) return;
    this.confirmar
      .confirmar(`¿Eliminar el periodo ${periodo.codigo}?`, 'Eliminar')
      .subscribe((ok) => {
        if (!ok) return;
        this.periodoService.eliminar(periodo.id as number).subscribe({
          next: () => this.cargar(),
          error: (err: HttpErrorResponse) => {
            if (err.status === 409) {
              this.error.set(err.error?.message ?? 'No se puede eliminar este periodo.');
            } else {
              this.error.set('No se pudo eliminar el periodo.');
            }
          },
        });
      });
  }

  protected aparienciaEstado(estado?: EstadoPeriodo): string {
    switch (estado) {
      case 'VIGENTE':
        return 'positive';
      case 'CERRADO':
        return 'neutral';
      default:
        return 'primary';
    }
  }
}
