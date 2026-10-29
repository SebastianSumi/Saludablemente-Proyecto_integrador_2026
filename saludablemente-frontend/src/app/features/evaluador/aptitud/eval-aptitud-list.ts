import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  TuiButton,
  TuiDropdown,
  TuiFilterByInputPipe,
  TuiIcon,
  TuiInput,
  TuiTextfield,
  tuiItemsHandlersProvider,
} from '@taiga-ui/core';
import { TuiChevron, TuiComboBox, TuiDataListWrapper } from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { AptitudService } from '../../admin/aptitud/aptitud-service';
import {
  EstadoEvaluacion,
  EvaluacionFiltros,
  EvaluacionResumen,
  Pagina,
} from '../../admin/aptitud/models';

interface FiltroOpcion {
  id: string;
  label: string;
}

@Component({
  selector: 'app-eval-aptitud-list',
  standalone: true,
  imports: [
    TuiTable,
    RouterLink,
    FormsModule,
    ReactiveFormsModule,
    TuiButton,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown,
    TuiFilterByInputPipe,
    TuiIcon,
    TuiInput,
    TuiTextfield,
  ],
  templateUrl: './eval-aptitud-list.html',
  styleUrls: ['../evaluador-comun.css'],
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
export class EvalAptitudList implements OnInit {
  private readonly aptitudService = inject(AptitudService);

  protected readonly pagina = signal<Pagina<EvaluacionResumen> | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);

  protected filtroEstado: EstadoEvaluacion | '' = '';
  protected filtroTexto = '';
  protected paginaActual = 0;

  protected readonly estadoOpts: FiltroOpcion[] = [
    { id: '', label: 'Todos los estados' },
    { id: 'PENDIENTE', label: 'Pendiente' },
    { id: 'COMPLETA', label: 'Completa' },
    { id: 'ANULADA', label: 'Anulada' },
  ];

  protected readonly estadoCtrl = new FormControl<FiltroOpcion | null>(null);

  ngOnInit(): void {
    this.estadoCtrl.setValue(this.estadoOpts[0], { emitEvent: false });
    this.estadoCtrl.valueChanges.subscribe((o) => {
      this.filtroEstado = (o?.id ?? '') as EstadoEvaluacion | '';
      this.aplicarFiltros();
    });
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);

    const filtros: EvaluacionFiltros = {
      page: this.paginaActual,
      size: 20,
      estado: this.filtroEstado === '' ? undefined : this.filtroEstado,
      texto: this.filtroTexto.trim() || undefined,
    };

    this.aptitudService.buscar(filtros).subscribe({
      next: (data) => this.pagina.set(data),
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 0
            ? 'No se pudo conectar con el servidor.'
            : 'No se pudo cargar la lista de evaluaciones.'
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

  limpiar(): void {
    this.filtroTexto = '';
    this.estadoCtrl.setValue(this.estadoOpts[0], { emitEvent: false });
    this.filtroEstado = '';
    this.aplicarFiltros();
  }

  irAPagina(n: number): void {
    if (n < 0) return;
    if (this.pagina() && n >= this.pagina()!.totalPages) return;
    this.paginaActual = n;
    this.cargar();
  }

  protected pillEstado(estado: string): string {
    if (estado === 'COMPLETA') return 'pill pill-success';
    if (estado === 'ANULADA') return 'pill pill-neutral';
    return 'pill pill-warning';
  }

  protected pillDictamen(dictamen?: string): string {
    switch (dictamen) {
      case 'APTO':
        return 'pill pill-success';
      case 'APTO_CON_RESTRICCIONES':
        return 'pill pill-warning';
      case 'NO_APTO':
        return 'pill pill-error';
      default:
        return 'pill pill-neutral';
    }
  }
}
