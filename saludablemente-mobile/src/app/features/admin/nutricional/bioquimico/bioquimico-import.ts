import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import {
  TuiButton,
  TuiDropdown,
  TuiFilterByInputPipe,
  TuiIcon,
  TuiLabel,
  TuiTextfield,
  tuiItemsHandlersProvider,
} from '@taiga-ui/core';
import {
  TuiButtonLoading,
  TuiChevron,
  TuiComboBox,
  TuiDataListWrapper,
} from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { BioquimicoService } from './bioquimico-service';
import { ImportacionReporte } from './bioquimico.model';
import { PeriodoService } from '../../catalogo/periodo/periodo-service';

export interface OpcionPeriodo {
  id: number;
  label: string;
  codigo: string;
}

@Component({
  selector: 'app-bioquimico-import',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    TuiButton,
    TuiButtonLoading,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown,
    TuiFilterByInputPipe,
    TuiIcon,
    TuiLabel,
    TuiTextfield,
    TuiTable,
  ],
  templateUrl: './bioquimico-import.html',
  styleUrl: './bioquimico-import.css',
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal((item: unknown): string => {
        if (item === null || item === undefined) return '';
        if (typeof item === 'string') return item;
        const o = item as { label?: string };
        return o.label ?? '';
      }),
      identityMatcher: signal((a: unknown, b: unknown): boolean => {
        if (a === b) return true;
        if (a === null || a === undefined || b === null || b === undefined) return false;
        const x = a as { codigo?: string };
        const y = b as { codigo?: string };
        return x.codigo === y.codigo;
      }),
    }),
  ],
})
export class BioquimicoImport {
  private readonly bioquimicoService = inject(BioquimicoService);
  private readonly periodoService = inject(PeriodoService);

  protected readonly periodos = signal<OpcionPeriodo[]>([]);
  protected readonly reporte = signal<ImportacionReporte | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly cargandoPeriodos = signal(true);

  protected archivoSeleccionado: File | null = null;
  protected periodoSeleccionado: OpcionPeriodo | null = null;

  constructor() {
    this.cargarPeriodos();
  }

  cargarPeriodos(): void {
    this.cargandoPeriodos.set(true);
    this.periodoService.listar().subscribe({
      next: (data) => {
        this.periodos.set(
          data
            .filter((p) => p.id !== undefined && p.estado !== 'CERRADO')
            .map((p) => ({
              id: p.id!,
              label: `${p.codigo} — ${p.descripcion}`,
              codigo: p.codigo,
            }))
        );
        this.cargandoPeriodos.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar los periodos.');
        this.cargandoPeriodos.set(false);
      },
    });
  }

  onArchivoSeleccionado(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.archivoSeleccionado = input.files[0];
      this.error.set(null);
      this.reporte.set(null);
    }
  }

  importar(): void {
    if (!this.archivoSeleccionado) {
      this.error.set('Seleccione un archivo Excel.');
      return;
    }
    if (!this.periodoSeleccionado) {
      this.error.set('Seleccione un periodo de evaluación.');
      return;
    }

    this.error.set(null);
    this.reporte.set(null);
    this.loading.set(true);

    this.bioquimicoService
      .importarExcel(this.archivoSeleccionado, this.periodoSeleccionado.codigo)
      .subscribe({
        next: (reporte) => {
          this.reporte.set(reporte);
          this.loading.set(false);
        },
        error: (err) => {
          if (err.status === 400) {
            this.error.set(
              err.error?.message ?? 'Archivo inválido o formato de columnas incompatible.'
            );
          } else {
            this.error.set('No se pudo procesar la importación del archivo.');
          }
          this.loading.set(false);
        },
      });
  }

  limpiar(): void {
    this.archivoSeleccionado = null;
    this.periodoSeleccionado = null;
    this.reporte.set(null);
    this.error.set(null);
    const input = document.getElementById('archivo') as HTMLInputElement;
    if (input) input.value = '';
  }
}
