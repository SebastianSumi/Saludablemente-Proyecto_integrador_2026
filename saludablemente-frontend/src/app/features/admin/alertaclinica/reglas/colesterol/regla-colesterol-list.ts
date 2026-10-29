import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
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
  TuiBadge,
  TuiChevron,
  TuiComboBox,
  TuiDataListWrapper,
} from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { ReglaColesterolService } from './regla-colesterol-service';
import {
  ReglaColesterolResponse,
  ReglaColesterolFiltros,
  EstadoRegla,
  SexoBiologico,
} from './regla-colesterol.model';

export interface OpcionFiltro {
  id: string;
  label: string;
}

import { imagenRegla } from '../../ilustracion-alerta';

@Component({
  selector: 'app-regla-colesterol-list',
  standalone: true,
  imports: [
    TuiTable,
    CommonModule,
    RouterLink,
    FormsModule,
    ReactiveFormsModule,
    TuiButton,
    TuiBadge,
    TuiLabel,
    TuiTextfield,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown,
    TuiFilterByInputPipe,
    TuiIcon,
  ],
  templateUrl: './regla-colesterol-list.html',
  styleUrls: ['./regla-colesterol-list.css'],
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal((item: unknown): string => {
        if (item === null || item === undefined) return '';
        if (typeof item === 'string') return item;
        return (item as OpcionFiltro).label ?? '';
      }),
      identityMatcher: signal((a: unknown, b: unknown): boolean => {
        if (a === b) return true;
        if (a === null || a === undefined || b === null || b === undefined) return false;
        return (a as OpcionFiltro).id === (b as OpcionFiltro).id;
      }),
    }),
  ],
})

export class ReglaColesterolList implements OnInit {
  protected readonly portada = imagenRegla('colesterol');
  private readonly service = inject(ReglaColesterolService);

  protected readonly items = signal<ReglaColesterolResponse[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly filtroEstado = signal<string>('ACTIVO');
  protected readonly filtroSexo = signal<string>('');

  protected readonly estadoOpts: OpcionFiltro[] = [
    { id: '', label: 'Todos los estados' },
    { id: 'ACTIVO', label: 'Activo' },
    { id: 'INACTIVO', label: 'Inactivo' },
  ];

  protected readonly sexoOpts: OpcionFiltro[] = [
    { id: '', label: 'Todos los sexos' },
    { id: 'M', label: 'Masculino' },
    { id: 'F', label: 'Femenino' },
  ];

  protected readonly estadoCtrl = new FormControl<OpcionFiltro | null>(this.estadoOpts[1]);
  protected readonly sexoCtrl = new FormControl<OpcionFiltro | null>(this.sexoOpts[0]);

  ngOnInit(): void {
    this.estadoCtrl.valueChanges.subscribe((o) => {
      this.filtroEstado.set(o?.id ?? '');
      this.cargar();
    });
    this.sexoCtrl.valueChanges.subscribe((o) => {
      this.filtroSexo.set(o?.id ?? '');
      this.cargar();
    });
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);

    const filtros: ReglaColesterolFiltros = {};
    const estado = this.filtroEstado();
    if (estado) filtros.estado = estado as EstadoRegla;
    const sexo = this.filtroSexo();
    if (sexo) filtros.sexo = sexo as SexoBiologico;

    this.service.listar(filtros).subscribe({
      next: (data) => this.items.set(data),
      error: (err: HttpErrorResponse) => {
        this.error.set(this.mapearError(err));
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  limpiarFiltros(): void {
    this.estadoCtrl.setValue(this.estadoOpts[0], { emitEvent: false });
    this.sexoCtrl.setValue(this.sexoOpts[0], { emitEvent: false });
    this.filtroEstado.set('');
    this.filtroSexo.set('');
    this.cargar();
  }

  desactivar(id: number): void {
    if (!confirm('¿Desea desactivar esta regla clínica?')) return;
    this.service.desactivar(id).subscribe({
      next: () => this.cargar(),
      error: (err: HttpErrorResponse) => this.error.set(this.mapearError(err)),
    });
  }

  reactivar(id: number): void {
    this.service.reactivar(id).subscribe({
      next: () => this.cargar(),
      error: (err: HttpErrorResponse) => this.error.set(this.mapearError(err)),
    });
  }

  private mapearError(err: HttpErrorResponse): string {
    if (err.status === 0) return 'No se pudo conectar con el servidor.';
    if (err.status === 404) return 'Regla no encontrada.';
    if (err.status === 409) return err.error?.message ?? 'Conflicto con otra regla existente.';
    if (err.status === 500) return 'Error interno del servidor.';
    return err.error?.message ?? 'Operación fallida.';
  }
}
