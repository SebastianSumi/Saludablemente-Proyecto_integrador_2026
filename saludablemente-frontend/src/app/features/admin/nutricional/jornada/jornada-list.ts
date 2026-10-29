import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
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
import { AuthService } from '../../../../core/auth/auth-service';
import { JornadaService } from './jornada-service';
import {
  EstadoJornada,
  JornadaEvaluacionResponse,
  Page,
} from './jornada.model';

interface FiltroOpcion {
  id: string;
  label: string;
}

@Component({
  selector: 'app-jornada-list',
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
    TuiTextfield,
  ],
  templateUrl: './jornada-list.html',
  styleUrl: './jornada-list.css',
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal(
        (item: unknown): string =>
          (item as { label?: string } | null)?.label ?? ''
      ),
      identityMatcher: signal(
        (a: unknown, b: unknown): boolean =>
          ((a as { id?: unknown } | null)?.id ?? null) ===
          ((b as { id?: unknown } | null)?.id ?? null)
      ),
    }),
  ],
})
export class JornadaList implements OnInit {
  private readonly jornadaService = inject(JornadaService);
  private readonly auth = inject(AuthService);

  protected readonly page = signal<Page<JornadaEvaluacionResponse> | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);

  protected readonly esAdmin = computed(() => this.auth.tieneRol('Administrador'));

  protected filtroEstado: EstadoJornada | '' = '';
  protected paginaActual = 0;

  protected readonly estadoOpts: FiltroOpcion[] = [
    { id: '', label: 'Todas' },
    { id: 'PROGRAMADA', label: 'Programada' },
    { id: 'ABIERTA', label: 'Abierta' },
    { id: 'CERRADA', label: 'Cerrada' },
    { id: 'CANCELADA', label: 'Cancelada' },
  ];

  protected readonly estadoCtrl = new FormControl<FiltroOpcion | null>(null);

  ngOnInit(): void {
    this.estadoCtrl.setValue(this.estadoOpts[0], { emitEvent: false });
    this.estadoCtrl.valueChanges.subscribe((opcion) => {
      this.filtroEstado = ((opcion?.id ?? '') as EstadoJornada | '');
      this.aplicarFiltros();
    });
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);

    this.jornadaService
      .buscar(
        this.filtroEstado === '' ? undefined : this.filtroEstado,
        undefined,
        this.paginaActual,
        30
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

  irAPagina(n: number): void {
    if (n < 0) return;
    if (this.page() && n >= this.page()!.totalPages) return;
    this.paginaActual = n;
    this.cargar();
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
