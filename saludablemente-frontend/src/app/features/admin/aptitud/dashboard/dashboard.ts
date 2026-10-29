import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import {
  TuiButton,
  TuiDropdown,
  TuiFilterByInputPipe,
  TuiIcon,
  TuiLabel,
  TuiTextfield,
  tuiItemsHandlersProvider,
} from '@taiga-ui/core';
import { TuiChevron, TuiComboBox, TuiDataListWrapper } from '@taiga-ui/kit';
import { catchError, of } from 'rxjs';
import { AptitudService } from '../aptitud-service';
import { PeriodoService } from '../../catalogo/periodo/periodo-service';
import { AptitudAgregado } from '../models';

export interface OpcionPeriodo {
  id: number;
  label: string;
  codigo: string;
}

@Component({
  selector: 'app-aptitud-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    ReactiveFormsModule,
    TuiButton,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown,
    TuiFilterByInputPipe,
    TuiIcon,
    TuiLabel,
    TuiTextfield,
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
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
export class Dashboard implements OnInit {
  private readonly service = inject(AptitudService);
  private readonly periodosSrv = inject(PeriodoService);

  protected readonly periodos = signal<OpcionPeriodo[]>([]);
  protected readonly agregado = signal<AptitudAgregado | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);

  protected readonly periodoCtrl = new FormControl<OpcionPeriodo | null>(null);

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
      if (vigente) {
        const opt: OpcionPeriodo = {
          id: vigente.id!,
          label: `${vigente.codigo} (${vigente.estado})`,
          codigo: vigente.codigo,
        };
        this.periodoCtrl.setValue(opt);
      }
    });

    this.periodoCtrl.valueChanges.subscribe(() => {
      this.cargar();
    });
  }

  cargar(): void {
    const periodo = this.periodoCtrl.value;
    if (!periodo?.id) {
      this.agregado.set(null);
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    this.service.agregados(periodo.id).subscribe({
      next: (data) => {
        this.agregado.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar los agregados de aptitud física.');
        this.loading.set(false);
      },
    });
  }

  limpiar(): void {
    this.periodoCtrl.setValue(null);
  }

  protected porcentaje(valor: number, total: number): string {
    if (!total || total === 0) return '0%';
    return `${((valor / total) * 100).toFixed(1)}%`;
  }
}
