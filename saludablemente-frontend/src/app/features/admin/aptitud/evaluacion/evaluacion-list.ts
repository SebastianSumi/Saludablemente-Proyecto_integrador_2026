import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { TuiButton, TuiDropdown, TuiFilterByInputPipe, TuiIcon, TuiInput, TuiLabel, TuiTextfield, tuiItemsHandlersProvider } from '@taiga-ui/core';
import {
  TuiAvatar,
  TuiBadge,
  TuiChevron,
  TuiComboBox,
  TuiDataListWrapper,
} from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AptitudService } from '../aptitud-service';
import { PeriodoService } from '../../catalogo/periodo/periodo-service';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { EstadoEvaluacion, EvaluacionResumen } from '../models';
import { Periodo } from '../../catalogo/periodo/periodo.model';

export interface OpcionPeriodo {
  id: number;
  label: string;
  codigo: string;
}

export interface OpcionEstado {
  id: string;
  label: string;
}

@Component({
  selector: 'app-evaluacion-list',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    TuiAvatar,
    TuiButton,
    TuiBadge,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown,
    TuiFilterByInputPipe,
    TuiIcon,
    TuiInput,
    TuiLabel,
    TuiTable,
    TuiTextfield,
  ],
  templateUrl: './evaluacion-list.html',
  styleUrl: './evaluacion-list.css',
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
        const x = a as { id?: unknown };
        const y = b as { id?: unknown };
        return x.id === y.id;
      }),
    }),
  ],
})
export class EvaluacionList implements OnInit {
  private readonly service = inject(AptitudService);
  private readonly periodosSrv = inject(PeriodoService);
  private readonly confirmar = inject(ConfirmService);

  protected readonly items = signal<EvaluacionResumen[]>([]);
  protected readonly periodos = signal<OpcionPeriodo[]>([]);
  protected readonly total = signal(0);
  protected readonly totalPages = signal(1);
  protected readonly page = signal(0);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly estadoOpts: OpcionEstado[] = [
    { id: '', label: 'Todos los estados' },
    { id: 'PENDIENTE', label: 'Pendiente' },
    { id: 'COMPLETA', label: 'Completa' },
    { id: 'ANULADA', label: 'Anulada' },
  ];

  protected readonly periodoCtrl = new FormControl<OpcionPeriodo | null>(null);
  protected readonly estadoCtrl = new FormControl<OpcionEstado | null>(null);
  protected readonly textoCtrl = new FormControl<string>('', { nonNullable: true });

  ngOnInit(): void {
    this.periodosSrv.listar().pipe(catchError(() => of([]))).subscribe((p) => {
      const mapeados: OpcionPeriodo[] = p
        .filter((x) => x.id !== undefined)
        .map((x) => ({
          id: x.id!,
          label: `${x.codigo} (${x.estado})`,
          codigo: x.codigo,
        }));
      this.periodos.set(mapeados);
    });

    this.periodosSrv.obtenerVigente().pipe(catchError(() => of(null))).subscribe((vigente) => {
      if (vigente && !this.periodoCtrl.value) {
        const vigenteOpcion: OpcionPeriodo = {
          id: vigente.id!,
          label: `${vigente.codigo} (${vigente.estado})`,
          codigo: vigente.codigo,
        };
        this.periodoCtrl.setValue(vigenteOpcion, { emitEvent: false });
        this.cargar();
      }
    });

    this.periodoCtrl.valueChanges.subscribe(() => {
      this.page.set(0);
      this.cargar();
    });

    this.estadoCtrl.valueChanges.subscribe(() => {
      this.page.set(0);
      this.cargar();
    });

    this.cargar();
  }

  aplicarTexto(): void {
    this.page.set(0);
    this.cargar();
  }

  limpiarFiltros(): void {
    this.periodoCtrl.setValue(null, { emitEvent: false });
    this.estadoCtrl.setValue(null, { emitEvent: false });
    this.textoCtrl.setValue('', { emitEvent: false });
    this.page.set(0);
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    const texto = this.textoCtrl.value.trim();
    this.service
      .buscar({
        idPeriodo: this.periodoCtrl.value?.id,
        estado: (this.estadoCtrl.value?.id || undefined) as EstadoEvaluacion | undefined,
        texto: texto || undefined,
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
            err.status === 0 ? 'No se pudo conectar con el servidor.' : 'No se pudo cargar las evaluaciones.'
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

  anular(e: EvaluacionResumen): void {
    if (!e.id) return;
    this.confirmar
      .confirmar(`¿Anular la evaluación de ${e.nombresCompletosAlMomento ?? e.id}?`, 'Anular')
      .subscribe((ok) => {
        if (!ok) return;
        this.service.anular(e.id as number).subscribe({
          next: () => this.cargar(),
          error: (err: HttpErrorResponse) => {
            this.error.set(err.error?.message ?? 'No se pudo anular la evaluación.');
          },
        });
      });
  }

  protected iniciales(nombre?: string): string {
    if (!nombre) return '?';
    return nombre
      .split(' ')
      .filter((x) => x.length > 0)
      .slice(0, 2)
      .map((x) => x.charAt(0).toUpperCase())
      .join('');
  }

  protected clasificacionFitness(score?: number | null): { label: string; clase: string } {
    if (score === undefined || score === null) return { label: 'Sin evaluar', clase: 'pill-soft-neutral' };
    if (score >= 85) return { label: 'Excelente', clase: 'pill-soft-success' };
    if (score >= 70) return { label: 'Muy Bueno', clase: 'pill-soft-success' };
    if (score >= 50) return { label: 'Bueno', clase: 'pill-soft-primary' };
    if (score >= 30) return { label: 'Regular', clase: 'pill-soft-warning' };
    return { label: 'Bajo', clase: 'pill-soft-error' };
  }

  protected aparienciaEstado(e?: string): string {
    switch (e) {
      case 'COMPLETA':
        return 'positive';
      case 'ANULADA':
        return 'negative';
      default:
        return 'warning';
    }
  }

  protected aparienciaDictamen(d?: string): string {
    switch (d) {
      case 'APTO':
        return 'positive';
      case 'APTO_CON_RESTRICCIONES':
        return 'warning';
      case 'NO_APTO':
        return 'negative';
      default:
        return 'neutral';
    }
  }
}
