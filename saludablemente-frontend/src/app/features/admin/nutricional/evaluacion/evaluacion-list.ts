import { Component, OnInit, inject, signal } from '@angular/core';
import { TuiButton, TuiDropdown, TuiFilterByInputPipe, TuiIcon, TuiInput, TuiLabel, TuiTextfield, tuiItemsHandlersProvider } from '@taiga-ui/core';
import { TuiBadge, TuiChevron, TuiComboBox, TuiDataListWrapper } from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { EvaluacionService } from './evaluacion-service';
import { ConfirmService } from '../../../../core/services/confirm-service';
import {
  EstadoEvaluacion,
  EvaluacionResumen,
  Page,
} from './evaluacion.model';

interface FiltroOpcion {
  id: string;
  label: string;
}

@Component({
  selector: 'app-evaluacion-list',
  imports: [TuiTable, RouterLink, FormsModule, ReactiveFormsModule, TuiButton, TuiBadge, TuiIcon, TuiLabel, TuiTextfield, TuiInput, TuiChevron, TuiComboBox, TuiDataListWrapper, TuiDropdown, TuiFilterByInputPipe],
  templateUrl: './evaluacion-list.html',
  styleUrl: './evaluacion-list.css',
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
export class EvaluacionList implements OnInit {
  private readonly evaluacionService = inject(EvaluacionService);
  private readonly confirmar = inject(ConfirmService);

  protected readonly page = signal<Page<EvaluacionResumen> | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);

  protected filtroEstado: EstadoEvaluacion | '' = '';
  protected filtroPeriodo = '';
  protected paginaActual = 0;

  protected readonly estadoOpts: FiltroOpcion[] = [
    { id: '', label: 'Todos' },
    { id: 'PENDIENTE', label: 'Pendiente' },
    { id: 'COMPLETA', label: 'Completa' },
    { id: 'ANULADA', label: 'Anulada' },
  ];

  protected readonly estadoCtrl = new FormControl<FiltroOpcion | null>(null);

  ngOnInit(): void {
    this.sincronizarControl();
    this.estadoCtrl.valueChanges.subscribe((o) => {
      this.filtroEstado = ((o?.id ?? '') as EstadoEvaluacion | '');
      this.aplicarFiltros();
    });
    this.cargar();
  }

  private sincronizarControl(): void {
    this.estadoCtrl.setValue(
      this.estadoOpts.find((o) => o.id === this.filtroEstado) ?? null,
      { emitEvent: false }
    );
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);

    this.evaluacionService.buscar(
      this.filtroEstado === '' ? undefined : this.filtroEstado,
      this.filtroPeriodo || undefined,
      undefined,
      this.paginaActual,
      20
    ).subscribe({
      next: (data) => this.page.set(data),
      error: () => {
        this.error.set('No se pudo cargar la lista de evaluaciones.');
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

  anular(id: number): void {
    this.confirmar
      .confirmar(`¿Anular la evaluación ${id}? Esta acción no se puede deshacer.`, 'Anular')
      .subscribe((ok) => {
        if (!ok) return;
        this.evaluacionService.anular(id).subscribe({
          next: () => this.cargar(),
          error: (err: HttpErrorResponse) => {
            if (err.status === 409) {
              this.error.set(err.error?.message ?? 'La evaluación ya está anulada.');
            } else {
              this.error.set('No se pudo anular la evaluación.');
            }
          },
        });
      });
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

  protected claseEstado(estado: EstadoEvaluacion): string {
    switch (estado) {
      case 'COMPLETA': return 'badge badge-completa';
      case 'ANULADA': return 'badge badge-anulada';
      default: return 'badge badge-pendiente';
    }
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
