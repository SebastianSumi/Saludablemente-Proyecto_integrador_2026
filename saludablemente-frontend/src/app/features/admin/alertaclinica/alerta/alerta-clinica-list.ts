import { EtiquetasClinicas } from '../../../../shared/components/etiquetas-clinicas/etiquetas-clinicas';
import { ClinicaNav } from '../etiquetas/clinica-nav';
import { EtiquetaClinicaService } from '../etiquetas/etiqueta-service';
import { EtiquetaResumen } from '../etiquetas/etiqueta.model';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subscription } from 'rxjs';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { TuiButton, TuiDropdown, TuiFilterByInputPipe, TuiIcon, TuiInput, TuiLabel, TuiTextfield, tuiItemsHandlersProvider } from '@taiga-ui/core';
import { TuiBadge, TuiChevron, TuiComboBox, TuiDataListWrapper } from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AlertaClinicaService } from './alerta-clinica-service';
import { PersonaNombreService } from './persona-nombre-service';
import {
  AlertaClinica,
  AlertaFiltros,
  EstadoAlerta,
  SeveridadAlerta,
  TipoEvaluacionOrigen,
} from './alerta-clinica.model';

interface FiltroOpcion {
  id: string;
  label: string;
}

import { imagenPortada } from '../ilustracion-alerta';

@Component({
  selector: 'app-alerta-clinica-list',
  standalone: true,
  imports: [EtiquetasClinicas,ClinicaNav, TuiTable, CommonModule, RouterLink, FormsModule, ReactiveFormsModule, TuiButton, TuiBadge, TuiChevron, TuiComboBox, TuiDataListWrapper, TuiDropdown, TuiFilterByInputPipe, TuiIcon, TuiInput, TuiLabel, TuiTextfield],
  templateUrl: './alerta-clinica-list.html',
  styleUrls: ['./alerta-clinica-list.css'],
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

export class AlertaClinicaList implements OnInit {
  protected readonly portada = imagenPortada();
  private readonly service = inject(AlertaClinicaService);
  private readonly nombresSrv = inject(PersonaNombreService);

  private readonly etiquetasService = inject(EtiquetaClinicaService);
  private readonly destroyRef = inject(DestroyRef);
  private consulta?: Subscription;
  readonly etiquetasFiltro = signal<EtiquetaResumen[]>([]);
  readonly etiquetaFiltroError = signal('');
  filtroEtiqueta = '';
  cargarEtiquetas(): void {
    this.etiquetaFiltroError.set('');
    this.etiquetasService.todas().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: e => this.etiquetasFiltro.set(e), error: () => this.etiquetaFiltroError.set('No se pudieron cargar las etiquetas del filtro.'),
    });
  }
  protected readonly items = signal<AlertaClinica[]>([]);
  protected readonly nombres = signal<Map<number, string>>(new Map());
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly filtroEstado = signal<string>('PENDIENTE');
  protected readonly filtroSeveridad = signal<string>('');
  protected readonly filtroTipo = signal<string>('');
  protected readonly filtroPersonaId = signal<number | null>(null);

  protected readonly estadoOpts: FiltroOpcion[] = [
    { id: '', label: 'Todos' },
    { id: 'PENDIENTE', label: 'Pendientes' },
    { id: 'ATENDIDA', label: 'Atendidas' },
    { id: 'ANULADA', label: 'Anuladas' },
  ];

  protected readonly severidadOpts: FiltroOpcion[] = [
    { id: '', label: 'Todas' },
    { id: 'LEVE', label: 'Leve' },
    { id: 'MODERADA', label: 'Moderada' },
    { id: 'CRITICA', label: 'Critica' },
  ];

  protected readonly tipoOpts: FiltroOpcion[] = [
    { id: '', label: 'Todos' },
    { id: 'NUTRICIONAL', label: 'Nutricional' },
    { id: 'APTITUD_FISICA', label: 'Aptitud Fisica' },
  ];

  protected readonly estadoCtrl = new FormControl<FiltroOpcion | null>(null);
  protected readonly severidadCtrl = new FormControl<FiltroOpcion | null>(null);
  protected readonly tipoCtrl = new FormControl<FiltroOpcion | null>(null);

  ngOnInit(): void {
    this.cargarEtiquetas();
    this.sincronizarControles();
    this.estadoCtrl.valueChanges.subscribe((o) => {
      this.filtroEstado.set(o?.id ?? '');
      this.cargar();
    });
    this.severidadCtrl.valueChanges.subscribe((o) => {
      this.filtroSeveridad.set(o?.id ?? '');
      this.cargar();
    });
    this.tipoCtrl.valueChanges.subscribe((o) => {
      this.filtroTipo.set(o?.id ?? '');
      this.cargar();
    });
    this.cargar();
  }

  private sincronizarControles(): void {
    this.estadoCtrl.setValue(
      this.estadoOpts.find((o) => o.id === this.filtroEstado()) ?? null,
      { emitEvent: false }
    );
    this.severidadCtrl.setValue(
      this.severidadOpts.find((o) => o.id === this.filtroSeveridad()) ?? null,
      { emitEvent: false }
    );
    this.tipoCtrl.setValue(
      this.tipoOpts.find((o) => o.id === this.filtroTipo()) ?? null,
      { emitEvent: false }
    );
  }

  protected nombrePersona(idPersona: number): string {
    return this.nombresSrv.nombreDe(this.nombres(), idPersona);
  }

  cargar(): void {
    this.consulta?.unsubscribe();
    this.loading.set(true);
    this.error.set(null);

    this.nombresSrv.nombres().subscribe({
      next: (m) => this.nombres.set(m),
      error: () => undefined,
    });

    const filtros: AlertaFiltros = {
      etiqueta: this.filtroEtiqueta || undefined,
      ordenarPor: 'fechaGeneracion',
      direccion: 'DESC',
    };

    const estado = this.filtroEstado();
    if (estado) filtros.estado = estado as EstadoAlerta;

    const severidad = this.filtroSeveridad();
    if (severidad) filtros.severidad = severidad as SeveridadAlerta;

    const tipo = this.filtroTipo();
    if (tipo) filtros.tipoOrigen = tipo as TipoEvaluacionOrigen;

    const personaId = this.filtroPersonaId();
    if (personaId) filtros.personaId = personaId;

    this.consulta = this.service.listar(filtros).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (data) => this.items.set(data),
      error: (err: HttpErrorResponse) => {
        this.error.set(this.mapearError(err));
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  limpiarFiltros(): void {
    this.filtroEtiqueta = '';
    this.filtroEstado.set('PENDIENTE');
    this.filtroSeveridad.set('');
    this.filtroTipo.set('');
    this.filtroPersonaId.set(null);
    this.estadoCtrl.setValue(
      this.estadoOpts.find((o) => o.id === 'PENDIENTE') ?? null,
      { emitEvent: false }
    );
    this.severidadCtrl.setValue(
      this.severidadOpts.find((o) => o.id === '') ?? null,
      { emitEvent: false }
    );
    this.tipoCtrl.setValue(
      this.tipoOpts.find((o) => o.id === '') ?? null,
      { emitEvent: false }
    );
    this.cargar();
  }

  private mapearError(err: HttpErrorResponse): string {
    if (err.status === 0) return 'No se pudo conectar con el servidor.';
    if (err.status === 404) return 'No se encontro el recurso.';
    if (err.status === 500) return 'Error interno del servidor.';
    return err.error?.message ?? 'No se pudo cargar la lista.';
  }

  protected severidadClass(sev: string): string {
    switch (sev) {
      case 'CRITICA': return 'negative';
      case 'MODERADA': return 'neutral';
      case 'LEVE': return 'positive';
      default: return 'primary';
    }
  }

  protected estadoClass(estado: string): string {
    switch (estado) {
      case 'PENDIENTE': return 'primary';
      case 'ATENDIDA': return 'positive';
      case 'ANULADA': return 'negative';
      default: return 'primary';
    }
  }
}
