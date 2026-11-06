import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { catchError, forkJoin, of } from 'rxjs';
import {
  TuiButton,
  TuiDropdown,
  TuiFilterByInputPipe,
  TuiIcon,
  TuiInput,
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
import { ConfirmService } from '../../../../core/services/confirm-service';
import { JornadaService } from './jornada-service';
import {
  EstadoInscripcion,
  InscripcionJornadaResponse,
  JornadaEvaluacionResponse,
} from './jornada.model';

interface OpcionEstado {
  id: EstadoInscripcion | '';
  label: string;
}

@Component({
  selector: 'app-jornada-inscritos',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    TuiButton,
    TuiButtonLoading,
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
  templateUrl: './jornada-inscritos.html',
  styleUrl: './jornada-inscritos.css',
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal((item: unknown): string => {
        if (item === null || item === undefined) return '';
        const o = item as { label?: string };
        return o.label ?? '';
      }),
      identityMatcher: signal((a: unknown, b: unknown): boolean => {
        if (a === null || a === undefined || b === null || b === undefined) {
          return a === b;
        }
        const x = a as { id?: unknown };
        const y = b as { id?: unknown };
        return x.id === y.id;
      }),
    }),
  ],
})
export class JornadaInscritos implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly jornadaService = inject(JornadaService);
  private readonly confirmar = inject(ConfirmService);

  protected readonly jornada = signal<JornadaEvaluacionResponse | null>(null);
  protected readonly inscripciones = signal<InscripcionJornadaResponse[]>([]);
  protected readonly filtroEstado = signal<EstadoInscripcion | ''>('');
  protected readonly busqueda = signal('');
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly procesando = signal<number | null>(null);

  protected readonly estadoOpts: OpcionEstado[] = [
    { id: '', label: 'Todos' },
    { id: 'ACTIVA', label: 'Activas' },
    { id: 'ASISTIO', label: 'Asistieron' },
    { id: 'CANCELADA', label: 'Canceladas' },
  ];

  protected readonly estadoCtrl = new FormControl<OpcionEstado | null>(null);

  protected readonly filtradas = computed(() => {
    const estado = this.filtroEstado();
    const texto = this.busqueda().trim().toLowerCase();

    return this.inscripciones().filter((i) => {
      if (estado && i.estado !== estado) return false;
      if (!texto) return true;
      return (
        (i.nombresCompletos ?? '').toLowerCase().includes(texto) ||
        (i.documentoIdentidad ?? '').toLowerCase().includes(texto)
      );
    });
  });

  protected readonly totalActivas = computed(
    () => this.inscripciones().filter((i) => i.estado === 'ACTIVA').length
  );
  protected readonly totalAsistieron = computed(
    () => this.inscripciones().filter((i) => i.estado === 'ASISTIO').length
  );
  protected readonly totalCanceladas = computed(
    () => this.inscripciones().filter((i) => i.estado === 'CANCELADA').length
  );
  protected readonly totalVigentes = computed(
    () => this.inscripciones().filter((i) => i.estado !== 'CANCELADA').length
  );

  private idJornada = 0;

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      this.error.set('ID de jornada no válido.');
      return;
    }
    this.idJornada = Number(idParam);

    this.estadoCtrl.setValue(this.estadoOpts[0], { emitEvent: false });
    this.estadoCtrl.valueChanges.subscribe((opcion) =>
      this.filtroEstado.set(opcion?.id ?? '')
    );

    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);

    forkJoin({
      jornada: this.jornadaService.obtener(this.idJornada),
      inscripciones: this.jornadaService.listarInscritos(this.idJornada),
    }).subscribe({
      next: ({ jornada, inscripciones }) => {
        this.jornada.set(jornada);
        this.inscripciones.set(inscripciones);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 409
            ? 'No tienes permisos para ver los inscritos de esta jornada.'
            : 'No se pudieron cargar los inscritos.'
        );
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  filtrarEstado(opcion: OpcionEstado | null): void {
    this.filtroEstado.set(opcion?.id ?? '');
  }

  actualizarBusqueda(valor: string): void {
    this.busqueda.set(valor);
  }

  cancelarInscripcion(i: InscripcionJornadaResponse): void {
    this.cambiarEstado(
      i,
      'CANCELADA',
      `¿Cancelar el turno ${i.numeroTurno} de ${i.nombresCompletos ?? 'esta persona'}? El cupo quedará libre.`,
      'Cancelar turno'
    );
  }

  reactivarInscripcion(i: InscripcionJornadaResponse): void {
    this.cambiarEstado(
      i,
      'ACTIVA',
      `¿Reactivar el turno ${i.numeroTurno} de ${i.nombresCompletos ?? 'esta persona'}? Se consume un cupo.`,
      'Reactivar turno'
    );
  }

  marcarAsistio(i: InscripcionJornadaResponse): void {
    this.cambiarEstado(
      i,
      'ASISTIO',
      `¿Marcar como asistió a ${i.nombresCompletos ?? 'esta persona'}?`,
      'Marcar asistió'
    );
  }

  private cambiarEstado(
    i: InscripcionJornadaResponse,
    estado: EstadoInscripcion,
    mensaje: string,
    accion: string
  ): void {
    if (this.procesando() !== null) return;

    this.confirmar.confirmar(mensaje, accion).subscribe((ok) => {
      if (!ok) return;

      this.procesando.set(i.idInscripcion);
      this.error.set(null);

      this.jornadaService
        .cambiarEstadoInscripcion(this.idJornada, i.idInscripcion, estado)
        .subscribe({
          next: () => {
            this.procesando.set(null);
            this.cargar();
          },
          error: (err: HttpErrorResponse) => {
            this.error.set(
              err.status === 409
                ? (err.error?.message ?? 'No se pudo actualizar la inscripción.')
                : 'No se pudo actualizar la inscripción.'
            );
            this.procesando.set(null);
          },
        });
    });
  }

  protected formatearFecha(fecha: string | null): string {
    if (!fecha) return '—';
    const [anio, mes, dia] = fecha.split('-');
    return `${dia}/${mes}/${anio}`;
  }

  protected formatearFechaHora(valor: string | null): string {
    if (!valor) return '—';
    const [fecha, hora] = valor.split('T');
    const f = this.formatearFecha(fecha);
    return hora ? `${f} ${hora.slice(0, 5)}` : f;
  }

  protected claseEstado(estado: EstadoInscripcion): string {
    switch (estado) {
      case 'ACTIVA':
        return 'pill pill-success';
      case 'ASISTIO':
        return 'pill pill-soft-primary';
      default:
        return 'pill pill-neutral';
    }
  }

  protected etiquetaEstado(estado: EstadoInscripcion): string {
    switch (estado) {
      case 'ACTIVA':
        return 'Activa';
      case 'ASISTIO':
        return 'Asistió';
      default:
        return 'Cancelada';
    }
  }
}
