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
import { TuiChevron, TuiComboBox, TuiDataListWrapper } from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { AlertaClinicaService } from '../../admin/alertaclinica/alerta/alerta-clinica-service';
import {
  AlertaClinica,
  AlertaFiltros,
  EstadoAlerta,
  SeveridadAlerta,
  TipoEvaluacionOrigen,
} from '../../admin/alertaclinica/alerta/alerta-clinica.model';

interface FiltroOpcion {
  id: string;
  label: string;
}

@Component({
  selector: 'app-eval-alertas-list',
  standalone: true,
  imports: [
    TuiTable,
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
  templateUrl: './eval-alertas-list.html',
  styleUrls: ['../evaluador-comun.css', '../evaluador-tablas-detalle.css'],
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
export class EvalAlertasList implements OnInit {
  private readonly service = inject(AlertaClinicaService);

  protected readonly alertas = signal<AlertaClinica[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  private filtroEstado: EstadoAlerta | '' = 'PENDIENTE';
  private filtroSeveridad: SeveridadAlerta | '' = '';
  private filtroTipo: TipoEvaluacionOrigen | '' = '';

  protected readonly estadoOpts: FiltroOpcion[] = [
    { id: '', label: 'Todas' },
    { id: 'PENDIENTE', label: 'Pendientes' },
    { id: 'ATENDIDA', label: 'Atendidas' },
    { id: 'ANULADA', label: 'Anuladas' },
  ];
  protected readonly severidadOpts: FiltroOpcion[] = [
    { id: '', label: 'Todas' },
    { id: 'LEVE', label: 'Leve' },
    { id: 'MODERADA', label: 'Moderada' },
    { id: 'CRITICA', label: 'Crítica' },
  ];
  protected readonly tipoOpts: FiltroOpcion[] = [
    { id: '', label: 'Todos' },
    { id: 'NUTRICIONAL', label: 'Nutricional' },
    { id: 'APTITUD_FISICA', label: 'Aptitud física' },
  ];

  protected readonly estadoCtrl = new FormControl<FiltroOpcion | null>(null);
  protected readonly severidadCtrl = new FormControl<FiltroOpcion | null>(null);
  protected readonly tipoCtrl = new FormControl<FiltroOpcion | null>(null);

  ngOnInit(): void {
    this.estadoCtrl.setValue(this.estadoOpts[1], { emitEvent: false });
    this.severidadCtrl.setValue(this.severidadOpts[0], { emitEvent: false });
    this.tipoCtrl.setValue(this.tipoOpts[0], { emitEvent: false });

    this.estadoCtrl.valueChanges.subscribe((o) => {
      this.filtroEstado = (o?.id ?? '') as EstadoAlerta | '';
      this.cargar();
    });
    this.severidadCtrl.valueChanges.subscribe((o) => {
      this.filtroSeveridad = (o?.id ?? '') as SeveridadAlerta | '';
      this.cargar();
    });
    this.tipoCtrl.valueChanges.subscribe((o) => {
      this.filtroTipo = (o?.id ?? '') as TipoEvaluacionOrigen | '';
      this.cargar();
    });

    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);

    const filtros: AlertaFiltros = {
      ordenarPor: 'fechaGeneracion',
      direccion: 'DESC',
    };
    if (this.filtroEstado) filtros.estado = this.filtroEstado;
    if (this.filtroSeveridad) filtros.severidad = this.filtroSeveridad;
    if (this.filtroTipo) filtros.tipoOrigen = this.filtroTipo;

    this.service.listar(filtros).subscribe({
      next: (data) => this.alertas.set(data),
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 0
            ? 'No se pudo conectar con el servidor.'
            : 'No se pudo cargar la lista de alertas.'
        );
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  limpiar(): void {
    this.estadoCtrl.setValue(this.estadoOpts[1], { emitEvent: false });
    this.severidadCtrl.setValue(this.severidadOpts[0], { emitEvent: false });
    this.tipoCtrl.setValue(this.tipoOpts[0], { emitEvent: false });
    this.filtroEstado = 'PENDIENTE';
    this.filtroSeveridad = '';
    this.filtroTipo = '';
    this.cargar();
  }

  protected pillSeveridad(sev: SeveridadAlerta): string {
    return sev === 'CRITICA'
      ? 'pill pill-error'
      : sev === 'MODERADA'
        ? 'pill pill-warning'
        : 'pill pill-secondary';
  }

  protected pillEstado(estado: string): string {
    if (estado === 'ATENDIDA') return 'pill pill-success';
    if (estado === 'ANULADA') return 'pill pill-neutral';
    return 'pill pill-warning';
  }
}
