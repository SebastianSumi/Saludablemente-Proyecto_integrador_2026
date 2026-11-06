import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
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
import { TuiButtonLoading, TuiChevron, TuiComboBox, TuiDataListWrapper, TuiInputDate } from '@taiga-ui/kit';
import { fijarHoyEn, limpiarFechaEn } from '../../../../core/forms/fecha-transformers';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AptitudService } from '../aptitud-service';
import { PeriodoService } from '../../catalogo/periodo/periodo-service';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { PersonaService } from '../../personal/persona/persona-service';
import { Periodo } from '../../catalogo/periodo/periodo.model';
import { Persona } from '../../personal/persona/persona.model';

export interface OpcionPersona {
  id: number;
  label: string;
  documento: string;
}

export interface OpcionPeriodo {
  id: number;
  label: string;
  codigo: string;
}

@Component({
  selector: 'app-evaluacion-form',
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
  templateUrl: './evaluacion-form.html',
  styleUrl: './evaluacion-form.css',
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
export class EvaluacionForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(AptitudService);
  private readonly personasSrv = inject(PersonaService);
  private readonly periodosSrv = inject(PeriodoService);
  private readonly router = inject(Router);

  protected readonly personas = signal<OpcionPersona[]>([]);
  protected readonly periodos = signal<OpcionPeriodo[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly cargandoDatos = signal(true);
  protected readonly guardando = signal(false);

  protected readonly form = this.fb.nonNullable.group({
    persona: [null as OpcionPersona | null, [Validators.required]],
    periodo: [null as OpcionPeriodo | null, [Validators.required]],
    fechaEvaluacion: [new Date().toISOString().slice(0, 10), [Validators.required]],
  });

  ngOnInit(): void {
    this.cargarDatosIniciales();
  }

  cargarDatosIniciales(): void {
    this.cargandoDatos.set(true);
    this.error.set(null);

    forkJoin({
      pers: this.personasSrv.listar({ estado: true }).pipe(catchError(() => of([]))),
      per: this.periodosSrv.listar().pipe(catchError(() => of([]))),
      vigente: this.periodosSrv.obtenerVigente().pipe(catchError(() => of(null))),
    }).subscribe({
      next: ({ pers, per, vigente }) => {
        this.personas.set(
          pers.map((p) => ({
            id: p.id,
            label: `${p.documentoIdentidad} — ${[p.apellidoPaterno, p.apellidoMaterno, p.nombres].filter(Boolean).join(' ')}`,
            documento: p.documentoIdentidad,
          }))
        );

        const periodosMapeados: OpcionPeriodo[] = per
          .filter((p) => p.id !== undefined)
          .map((p) => ({
            id: p.id!,
            label: `${p.codigo} (${p.estado})`,
            codigo: p.codigo,
          }));
        this.periodos.set(periodosMapeados);

        if (vigente) {
          const vigenteOpcion = periodosMapeados.find((p) => p.id === vigente.id);
          if (vigenteOpcion) {
            this.form.patchValue({ periodo: vigenteOpcion });
          }
        } else if (periodosMapeados.length > 0) {
          this.form.patchValue({ periodo: periodosMapeados[0] });
        }

        this.cargandoDatos.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar los catálogos.');
        this.cargandoDatos.set(false);
      },
    });
  }

  guardar(): void {
    if (this.loading() || this.cargandoDatos() || this.guardando()) return;
    this.error.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Selecciona la persona, el periodo y la fecha.');
      return;
    }

    const v = this.form.getRawValue();
    const persona = v.persona;
    const periodo = v.periodo;

    if (!persona?.id || !periodo?.id) {
      this.error.set('Selecciona persona y periodo.');
      return;
    }

    this.loading.set(true);
    this.guardando.set(true);

    this.service
      .iniciar({
        idPersona: persona.id,
        idPeriodo: periodo.id,
        fechaEvaluacion: v.fechaEvaluacion,
      })
      .subscribe({
        next: (e) => this.router.navigate(['/aptitud', e.id]),
        error: (err: HttpErrorResponse) => {
          if (err.status === 409) {
            this.error.set(err.error?.message ?? 'La persona ya tiene una evaluación en este periodo.');
          } else if (err.status === 400) {
            this.error.set(err.error?.message ?? 'El periodo seleccionado debe ser el vigente.');
          } else {
            this.error.set(err.error?.message ?? 'No se pudo iniciar la evaluación.');
          }
          this.loading.set(false);
          this.guardando.set(false);
        },
      });
  }

  cancelar(): void {
    this.router.navigate(['/aptitud']);
  }

  protected fijarHoy(campo: string): void {
    fijarHoyEn(this.form.get(campo));
  }

  protected limpiarCampo(campo: string): void {
    limpiarFechaEn(this.form.get(campo));
  }
}
