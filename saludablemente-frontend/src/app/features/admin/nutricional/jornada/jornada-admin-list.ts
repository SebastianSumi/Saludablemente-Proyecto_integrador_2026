import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import {
  TuiButton,
  TuiDropdown,
  TuiFilterByInputPipe,
  TuiIcon,
  TuiTextfield,
  tuiItemsHandlersProvider,
} from '@taiga-ui/core';
import {
  TuiChevron,
  TuiComboBox,
  TuiDataListWrapper,
} from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { PeriodoService } from '../../catalogo/periodo/periodo-service';
import { Periodo } from '../../catalogo/periodo/periodo.model';
import { JornadaService } from './jornada-service';
import {
  EstadoJornada,
  JornadaEvaluacionResponse,
  Page,
} from './jornada.model';

interface Opcion {
  id: string;
  label: string;
}

interface OpcionPeriodo {
  id: number | null;
  label: string;
}

@Component({
  selector: 'app-jornada-admin-list',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    TuiButton,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown,
    TuiFilterByInputPipe,
    TuiIcon,
    TuiTable,
    TuiTextfield,
  ],
  templateUrl: './jornada-admin-list.html',
  styleUrl: './jornada-admin-list.css',
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal((item: unknown): string => {
        if (item === null || item === undefined) return '';
        const o = item as { label?: string };
        return o.label ?? '';
      }),
      identityMatcher: signal((a: unknown, b: unknown): boolean => {
        if (a === null || a === undefined || b === null || b === undefined) {
          return a === b;
        }
        const x = a as { id?: unknown };
        const y = b as { id?: unknown };
        return x.id === y.id;
      }),
    }),
  ],
})
export class JornadaAdminList implements OnInit {
  private readonly jornadaService = inject(JornadaService);
  private readonly periodoService = inject(PeriodoService);
  private readonly confirmar = inject(ConfirmService);

  protected readonly page = signal<Page<JornadaEvaluacionResponse> | null>(null);
  protected readonly periodos = signal<OpcionPeriodo[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);

  protected filtroEstado: EstadoJornada | '' = '';
  protected filtroPeriodo: number | null = null;
  protected paginaActual = 0;

  protected readonly estadoOpts: Opcion[] = [
    { id: '', label: 'Todos' },
    { id: 'PROGRAMADA', label: 'Programada' },
    { id: 'ABIERTA', label: 'Abierta' },
    { id: 'CERRADA', label: 'Cerrada' },
    { id: 'CANCELADA', label: 'Cancelada' },
  ];

  protected readonly estadoCtrl = new FormControl<Opcion | null>(null);
  protected readonly periodoCtrl = new FormControl<OpcionPeriodo | null>(null);

  ngOnInit(): void {
    this.estadoCtrl.setValue(this.estadoOpts[0], { emitEvent: false });
    this.periodoCtrl.setValue({ id: null, label: 'Todos los periodos' }, {
      emitEvent: false,
    });

    this.estadoCtrl.valueChanges.subscribe((opcion) => {
      this.filtroEstado = ((opcion?.id ?? '') as EstadoJornada | '');
      this.aplicarFiltros();
    });
    this.periodoCtrl.valueChanges.subscribe((opcion) => {
      this.filtroPeriodo = opcion?.id ?? null;
      this.aplicarFiltros();
    });

    this.cargarPeriodos();
    this.cargar();
  }

  private cargarPeriodos(): void {
    this.periodoService.listar().subscribe({
      next: (data: Periodo[]) => {
        this.periodos.set([
          { id: null, label: 'Todos los periodos' },
          ...data
            .filter((p) => p.id !== undefined)
            .map((p) => ({
              id: p.id!,
              label: `${p.codigo} — ${p.descripcion}`,
            })),
        ]);
      },
      error: () => {
        this.periodos.set([{ id: null, label: 'Todos los periodos' }]);
      },
    });
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);

    this.jornadaService
      .buscar(
        this.filtroEstado === '' ? undefined : this.filtroEstado,
        this.filtroPeriodo ?? undefined,
        this.paginaActual,
        20
      )
      .subscribe({
        next: (data) => this.page.set(data),
        error: (err: HttpErrorResponse) => {
          this.error.set(
            err.status === 0
              ? 'No se pudo conectar con el servidor.'
              : 'No se pudieron cargar las jornadas.'
          );
          this.loading.set(false);
        },
        complete: () => this.loading.set(false),
      });
  }

  aplicarFiltros(): void {
    this.paginaActual = 0;
    this.cargar();
  }

  limpiarFiltros(): void {
    this.estadoCtrl.setValue(this.estadoOpts[0], { emitEvent: false });
    this.periodoCtrl.setValue(
      { id: null, label: 'Todos los periodos' },
      { emitEvent: false }
    );
    this.filtroEstado = '';
    this.filtroPeriodo = null;
    this.aplicarFiltros();
  }

  irAPagina(n: number): void {
    if (n < 0) return;
    if (this.page() && n >= this.page()!.totalPages) return;
    this.paginaActual = n;
    this.cargar();
  }

  abrir(j: JornadaEvaluacionResponse): void {
    this.cambiarEstado(j, 'ABIERTA', '¿Abrir esta jornada para inscripciones?', 'Abrir');
  }

  cerrar(j: JornadaEvaluacionResponse): void {
    this.cambiarEstado(j, 'CERRADA', '¿Cerrar esta jornada? No se aceptarán nuevas inscripciones.', 'Cerrar');
  }

  cancelar(j: JornadaEvaluacionResponse): void {
    this.cambiarEstado(j, 'CANCELADA', '¿Cancelar esta jornada? Las inscripciones existentes se conservan.', 'Cancelar jornada');
  }

  private cambiarEstado(
    j: JornadaEvaluacionResponse,
    estado: EstadoJornada,
    mensaje: string,
    accion: string
  ): void {
    this.confirmar.confirmar(mensaje, accion).subscribe((ok) => {
      if (!ok) return;
      this.jornadaService.cambiarEstado(j.idJornada, estado).subscribe({
        next: () => this.cargar(),
        error: (err: HttpErrorResponse) => {
          this.error.set(
            err.status === 409
              ? (err.error?.message ?? 'No se pudo cambiar el estado.')
              : 'No se pudo cambiar el estado de la jornada.'
          );
        },
      });
    });
  }

  protected formatearFecha(fecha: string | null): string {
    if (!fecha) return '—';
    const [anio, mes, dia] = fecha.split('-');
    return `${dia}/${mes}/${anio}`;
  }

  protected formatearHora(hora: string | null): string {
    if (!hora) return '—';
    return hora.slice(0, 5);
  }

  protected claseEstado(estado: EstadoJornada): string {
    switch (estado) {
      case 'ABIERTA':
        return 'pill pill-success';
      case 'CERRADA':
        return 'pill pill-neutral';
      case 'CANCELADA':
        return 'pill pill-error';
      default:
        return 'pill pill-warning';
    }
  }

  protected etiquetaEstado(estado: EstadoJornada): string {
    switch (estado) {
      case 'ABIERTA':
        return 'Abierta';
      case 'CERRADA':
        return 'Cerrada';
      case 'CANCELADA':
        return 'Cancelada';
      default:
        return 'Programada';
    }
  }
}
