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
import { EvaluacionService } from '../../admin/nutricional/evaluacion/evaluacion-service';
import {
  EstadoEvaluacion,
  EvaluacionResumen,
  Page,
} from '../../admin/nutricional/evaluacion/evaluacion.model';

interface FiltroOpcion {
  id: string;
  label: string;
}

@Component({
  selector: 'app-eval-nutricional-list',
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
  templateUrl: './eval-nutricional-list.html',
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
export class EvalNutricionalList implements OnInit {
  private readonly evaluacionService = inject(EvaluacionService);

  protected readonly page = signal<Page<EvaluacionResumen> | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);

  protected filtroEstado: EstadoEvaluacion | '' = '';
  protected filtroPeriodo = '';
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

    this.evaluacionService
      .buscar(
        this.filtroEstado === '' ? undefined : this.filtroEstado,
        this.filtroPeriodo || undefined,
        undefined,
        this.paginaActual,
        20
      )
      .subscribe({
        next: (data) => this.page.set(data),
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
    this.filtroPeriodo = '';
    this.estadoCtrl.setValue(this.estadoOpts[0], { emitEvent: false });
    this.filtroEstado = '';
    this.aplicarFiltros();
  }

  irAPagina(n: number): void {
    if (n < 0) return;
    if (this.page() && n >= this.page()!.totalPages) return;
    this.paginaActual = n;
    this.cargar();
  }

  descargarPdf(id: number): void {
    this.evaluacionService.descargarPdf(id).subscribe({
      next: (blob) => this.guardarArchivo(blob, `ficha-${id}.pdf`),
      error: () => this.error.set('No se pudo generar la ficha PDF.'),
    });
  }

  exportarConsolidado(): void {
    if (!this.filtroPeriodo) {
      this.error.set('Seleccione un periodo para exportar el consolidado.');
      return;
    }
    this.evaluacionService.exportarConsolidado(this.filtroPeriodo).subscribe({
      next: (blob) => this.guardarArchivo(blob, `consolidado-${this.filtroPeriodo}.xlsx`),
      error: () => this.error.set('No se pudo generar el consolidado.'),
    });
  }

  private guardarArchivo(blob: Blob, nombre: string): void {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = nombre;
    a.click();
    window.URL.revokeObjectURL(url);
  }
}
