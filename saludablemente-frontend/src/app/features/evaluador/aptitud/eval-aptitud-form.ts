import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
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
import { fijarHoyEn, limpiarFechaEn } from '../../../core/forms/fecha-transformers';
import { AptitudService } from '../../admin/aptitud/aptitud-service';
import { PersonaService } from '../../admin/personal/persona/persona-service';
import { PeriodoService } from '../../admin/catalogo/periodo/periodo-service';

interface OpcionPersona {
  id: number;
  label: string;
}

interface OpcionPeriodo {
  id: number;
  label: string;
}

@Component({
  selector: 'app-eval-aptitud-form',
  standalone: true,
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
  templateUrl: './eval-aptitud-form.html',
  styleUrls: ['../evaluador-comun.css'],
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
export class EvalAptitudForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly aptitudService = inject(AptitudService);
  private readonly personasSrv = inject(PersonaService);
  private readonly periodosSrv = inject(PeriodoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly periodos = signal<OpcionPeriodo[]>([]);
  protected readonly personas = signal<OpcionPersona[]>([]);
  protected readonly error = signal<string | null>(null);
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

  private cargarDatosIniciales(): void {
    const personaParam = Number(this.route.snapshot.queryParamMap.get('persona')) || null;

    forkJoin({
      pers: this.personasSrv.listar({ estado: true }).pipe(catchError(() => of([]))),
      per: this.periodosSrv.listar().pipe(catchError(() => of([]))),
      vigente: this.periodosSrv.obtenerVigente().pipe(catchError(() => of(null))),
    }).subscribe({
      next: ({ pers, per, vigente }) => {
        const personasOpts = pers.map((p) => ({
          id: p.id,
          label: `${p.documentoIdentidad} — ${[p.apellidoPaterno, p.apellidoMaterno, p.nombres]
            .filter(Boolean)
            .join(' ')}`,
        }));
        this.personas.set(personasOpts);
        if (personaParam) {
          const elegida = personasOpts.find((o) => o.id === personaParam);
          if (elegida) this.form.patchValue({ persona: elegida });
        }

        const periodosOpts: OpcionPeriodo[] = per
          .filter((p) => p.id !== undefined)
          .map((p) => ({ id: p.id!, label: `${p.codigo} (${p.estado})` }));
        this.periodos.set(periodosOpts);
        if (vigente) {
          const vigenteOpcion = periodosOpts.find((p) => p.id === vigente.id);
          if (vigenteOpcion) this.form.patchValue({ periodo: vigenteOpcion });
        } else if (periodosOpts.length > 0) {
          this.form.patchValue({ periodo: periodosOpts[0] });
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
    if (this.guardando() || this.cargandoDatos()) return;
    this.error.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Selecciona la persona, el periodo y la fecha.');
      return;
    }

    const v = this.form.getRawValue();
    if (!v.persona?.id || !v.periodo?.id) {
      this.error.set('Selecciona persona y periodo.');
      return;
    }

    this.guardando.set(true);
    this.aptitudService
      .iniciar({
        idPersona: v.persona.id,
        idPeriodo: v.periodo.id,
        fechaEvaluacion: v.fechaEvaluacion,
      })
      .subscribe({
        next: (e) => this.router.navigate(['/evaluador/aptitud', e.id]),
        error: (err: HttpErrorResponse) => {
          if (err.status === 409) {
            this.error.set(err.error?.message ?? 'La persona ya tiene evaluación en ese periodo.');
          } else if (err.status === 400) {
            this.error.set(err.error?.message ?? 'El periodo seleccionado debe ser el vigente.');
          } else {
            this.error.set(err.error?.message ?? 'No se pudo iniciar la evaluación.');
          }
          this.guardando.set(false);
        },
      });
  }

  cancelar(): void {
    this.router.navigate(['/evaluador/aptitud']);
  }

  protected fijarHoy(): void {
    fijarHoyEn(this.form.get('fechaEvaluacion'));
  }

  protected limpiarFecha(): void {
    limpiarFechaEn(this.form.get('fechaEvaluacion'));
  }
}
