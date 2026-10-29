import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, forkJoin, of } from 'rxjs';
import {
  TuiButton,
  TuiCalendar,
  TuiDropdown,
  TuiError,
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
  TuiInputDate,
} from '@taiga-ui/kit';
import { fijarHoyEn, limpiarFechaEn } from '../../../../core/forms/fecha-transformers';
import { PeriodoService } from '../../catalogo/periodo/periodo-service';
import { Periodo } from '../../catalogo/periodo/periodo.model';
import { JornadaService } from './jornada-service';
import {
  EstadoJornada,
  JornadaEvaluacionRequest,
  JornadaEvaluacionResponse,
} from './jornada.model';

interface OpcionPeriodo {
  id: number;
  label: string;
}

interface OpcionEstado {
  id: EstadoJornada;
  label: string;
}

@Component({
  selector: 'app-jornada-form',
  imports: [
    ReactiveFormsModule,
    TuiButton,
    TuiButtonLoading,
    TuiCalendar,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown,
    TuiError,
    TuiFilterByInputPipe,
    TuiIcon,
    TuiInput,
    TuiInputDate,
    TuiLabel,
    TuiTextfield,
  ],
  templateUrl: './jornada-form.html',
  styleUrl: './jornada-form.css',
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
export class JornadaForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly jornadaService = inject(JornadaService);
  private readonly periodoService = inject(PeriodoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly id = signal<number | null>(null);
  protected readonly periodos = signal<OpcionPeriodo[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly cargando = signal(false);
  protected readonly guardando = signal(false);

  protected readonly estadoOpts: OpcionEstado[] = [
    { id: 'PROGRAMADA', label: 'Programada' },
    { id: 'ABIERTA', label: 'Abierta' },
    { id: 'CERRADA', label: 'Cerrada' },
    { id: 'CANCELADA', label: 'Cancelada' },
  ];

  protected readonly form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(150)]],
    descripcion: ['', [Validators.maxLength(500)]],
    fecha: ['', [Validators.required]],
    hora: ['', [Validators.required]],
    capacidad: [20, [Validators.required, Validators.min(1), Validators.max(10000)]],
    periodo: [null as OpcionPeriodo | null, [Validators.required]],
    estado: [this.estadoOpts[1] as OpcionEstado | null, [Validators.required]],
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const id = idParam ? Number(idParam) : null;
    this.id.set(id);
    this.cargarDatos(id);
  }

  private cargarDatos(id: number | null): void {
    this.cargando.set(true);
    this.error.set(null);

    forkJoin({
      periodos: this.periodoService.listar().pipe(catchError(() => of([] as Periodo[]))),
      jornada: id
        ? this.jornadaService.obtener(id).pipe(catchError(() => of(null)))
        : of(null),
    }).subscribe({
      next: ({ periodos, jornada }) => {
        this.periodos.set(
          periodos
            .filter((p) => p.id !== undefined && p.estado !== 'CERRADO')
            .map((p) => ({
              id: p.id!,
              label: `${p.codigo} — ${p.descripcion}`,
            }))
        );

        if (jornada) {
          this.aplicarJornada(jornada);
        }
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar los datos del formulario.');
        this.cargando.set(false);
      },
    });
  }

  private aplicarJornada(jornada: JornadaEvaluacionResponse): void {
    const periodo =
      this.periodos().find((p) => p.id === jornada.idPeriodo) ?? null;
    const estado =
      this.estadoOpts.find((e) => e.id === jornada.estado) ?? this.estadoOpts[1];

    this.form.patchValue({
      nombre: jornada.nombre,
      descripcion: jornada.descripcion ?? '',
      fecha: jornada.fecha,
      hora: (jornada.hora ?? '').slice(0, 5),
      capacidad: jornada.capacidad,
      periodo,
      estado,
    });
  }

  guardar(): void {
    if (this.cargando() || this.guardando()) return;

    this.error.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      if (!this.form.controls.periodo.value) {
        this.error.set('Seleccione un periodo para la jornada.');
      }
      return;
    }

    const valor = this.form.getRawValue();
    if (!valor.periodo) {
      this.error.set('Seleccione un periodo para la jornada.');
      return;
    }
    if (!valor.estado) {
      this.error.set('Seleccione un estado para la jornada.');
      return;
    }

    const request: JornadaEvaluacionRequest = {
      nombre: valor.nombre.trim(),
      descripcion: valor.descripcion?.trim() ? valor.descripcion.trim() : null,
      fecha: valor.fecha,
      hora: valor.hora,
      capacidad: Number(valor.capacidad),
      idPeriodo: valor.periodo.id,
      estado: valor.estado.id,
    };

    const id = this.id();
    const peticion = id
      ? this.jornadaService.actualizar(id, request)
      : this.jornadaService.crear(request);

    this.guardando.set(true);
    peticion.subscribe({
      next: () => this.router.navigate(['/admin/nutricional/jornadas']),
      error: (err: HttpErrorResponse) => {
        if (err.status === 409) {
          this.error.set(
            err.error?.message ?? 'No se pudo guardar la jornada por una regla de negocio.'
          );
        } else if (err.status === 404) {
          this.error.set(err.error?.message ?? 'El periodo seleccionado no existe.');
        } else {
          this.error.set('No se pudo guardar la jornada.');
        }
        this.guardando.set(false);
      },
    });
  }

  cancelar(): void {
    this.router.navigate(['/admin/nutricional/jornadas']);
  }

  protected fijarHoy(): void {
    fijarHoyEn(this.form.controls.fecha);
  }

  protected limpiarFecha(): void {
    limpiarFechaEn(this.form.controls.fecha);
  }

  protected mensajeValidacion(campo: 'nombre' | 'descripcion' | 'fecha' | 'hora' | 'capacidad'): string {
    const control = this.form.controls[campo];
    if (!control.touched) return '';

    if (control.hasError('required')) {
      return 'Este campo es obligatorio.';
    }
    if (control.hasError('maxlength')) {
      return `Máximo ${control.getError('maxlength').requiredLength} caracteres.`;
    }
    if (control.hasError('min')) {
      return `El valor mínimo es ${control.getError('min').min}.`;
    }
    if (control.hasError('max')) {
      return `El valor máximo es ${control.getError('max').max}.`;
    }
    return '';
  }
}
