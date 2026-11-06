import { Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  TuiButton,
  TuiDropdown,
  TuiFilterByInputPipe,
  TuiError,
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
import { EvaluacionService } from './evaluacion-service';
import { Periodo } from '../../catalogo/periodo/periodo.model';
import { PersonaService } from '../../personal/persona/persona-service';
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
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TuiButton,
    TuiButtonLoading,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown,
    TuiFilterByInputPipe,
    TuiError,
    TuiIcon,
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
  private readonly evaluacionService = inject(EvaluacionService);
  private readonly personaService = inject(PersonaService);
  private readonly router = inject(Router);

  protected readonly periodos = signal<OpcionPeriodo[]>([]);
  protected readonly personas = signal<OpcionPersona[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly cargandoDatos = signal(true);
  protected readonly guardando = signal(false);

  protected readonly form = this.fb.group({
    persona: [null as OpcionPersona | null, [Validators.required]],
    periodo: [null as OpcionPeriodo | null, [Validators.required]],
  });

  ngOnInit(): void {
    this.cargarDatosIniciales();
  }

  cargarDatosIniciales(): void {
    this.cargandoDatos.set(true);
    this.error.set(null);

    let pendientes = 2;

    const verificar = () => {
      if (--pendientes === 0) this.cargandoDatos.set(false);
    };

    this.evaluacionService.periodosDisponibles().subscribe({
      next: (data) => {
        this.periodos.set(
          data
            .filter((p) => p.id !== undefined)
            .map((p) => ({
              id: p.id!,
              label: `${p.codigo} — ${p.descripcion} (${p.estado})`,
              codigo: p.codigo,
            }))
        );
      },
      error: () => {
        this.error.set('No se pudieron cargar los periodos disponibles.');
        verificar();
      },
      complete: verificar,
    });

    // Solo personas ACTIVAS
    this.personaService.listar({ estado: true }).subscribe({
      next: (data) => {
        this.personas.set(
          data.map((p) => ({
            id: p.id,
            label: `${p.documentoIdentidad} — ${p.apellidoPaterno} ${p.apellidoMaterno ?? ''}, ${p.nombres}`.trim(),
            documento: p.documentoIdentidad,
          }))
        );
      },
      error: () => {
        this.error.set('No se pudo cargar la lista de personas.');
        verificar();
      },
      complete: verificar,
    });
  }

  guardar(): void {
    if (this.loading() || this.cargandoDatos() || this.guardando()) return;

    this.error.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valor = this.form.getRawValue();
    this.loading.set(true);
    this.guardando.set(true);

    this.evaluacionService.iniciar({
      idPersona: valor.persona!.id,
      idPeriodoId: valor.periodo!.id,
    }).subscribe({
      next: (evaluacion) =>
        this.router.navigate(['/nutricional/evaluaciones', evaluacion.idEvaluacion]),
      error: (err) => {
        if (err.status === 409) {
          this.error.set(
            err.error?.message ?? 'La persona ya tiene evaluación en ese periodo.'
          );
        } else if (err.status === 404) {
          this.error.set(err.error?.message ?? 'Persona o periodo no encontrado.');
        } else {
          this.error.set('No se pudo iniciar la evaluación.');
        }
        this.loading.set(false);
        this.guardando.set(false);
      },
    });
  }

  cancelar(): void {
    this.router.navigate(['/nutricional/evaluaciones']);
  }
}
