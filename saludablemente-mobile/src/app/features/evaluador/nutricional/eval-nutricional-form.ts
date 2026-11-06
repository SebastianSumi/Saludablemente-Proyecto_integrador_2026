import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  TuiButton,
  TuiDropdown,
  TuiError,
  TuiFilterByInputPipe,
  TuiIcon,
  TuiLabel,
  TuiTextfield,
  tuiItemsHandlersProvider,
} from '@taiga-ui/core';
import { TuiButtonLoading, TuiChevron, TuiComboBox, TuiDataListWrapper } from '@taiga-ui/kit';
import { EvaluacionService } from '../../admin/nutricional/evaluacion/evaluacion-service';
import { PersonaService } from '../../admin/personal/persona/persona-service';

interface OpcionPersona {
  id: number;
  label: string;
}

interface OpcionPeriodo {
  id: number;
  label: string;
}

@Component({
  selector: 'app-eval-nutricional-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TuiButton,
    TuiButtonLoading,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown,
    TuiError,
    TuiFilterByInputPipe,
    TuiIcon,
    TuiLabel,
    TuiTextfield,
  ],
  templateUrl: './eval-nutricional-form.html',
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
export class EvalNutricionalForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly evaluacionService = inject(EvaluacionService);
  private readonly personaService = inject(PersonaService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly periodos = signal<OpcionPeriodo[]>([]);
  protected readonly personas = signal<OpcionPersona[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly cargandoDatos = signal(true);
  protected readonly guardando = signal(false);

  protected readonly form = this.fb.group({
    persona: [null as OpcionPersona | null, [Validators.required]],
    periodo: [null as OpcionPeriodo | null, [Validators.required]],
  });

  ngOnInit(): void {
    this.cargarDatosIniciales();
  }

  private cargarDatosIniciales(): void {
    const personaParam = Number(this.route.snapshot.queryParamMap.get('persona')) || null;
    let pendientes = 2;
    const verificar = () => {
      if (--pendientes === 0) this.cargandoDatos.set(false);
    };

    this.evaluacionService.periodosDisponibles().subscribe({
      next: (data) => {
        const opts = data
          .filter((p) => p.id !== undefined)
          .map((p) => ({
            id: p.id!,
            label: `${p.codigo} — ${p.descripcion} (${p.estado})`,
          }));
        this.periodos.set(opts);
        const vigente = opts.find((o) => o.label.includes('VIGENTE'));
        if (vigente) this.form.patchValue({ periodo: vigente });
      },
      error: () => this.error.set('No se pudieron cargar los periodos disponibles.'),
      complete: verificar,
    });

    this.personaService.listar({ estado: true }).subscribe({
      next: (data) => {
        const opts = data.map((p) => ({
          id: p.id,
          label: `${p.documentoIdentidad} — ${p.apellidoPaterno} ${p.apellidoMaterno ?? ''}, ${p.nombres}`.trim(),
        }));
        this.personas.set(opts);
        if (personaParam) {
          const elegida = opts.find((o) => o.id === personaParam);
          if (elegida) this.form.patchValue({ persona: elegida });
        }
      },
      error: () => this.error.set('No se pudo cargar la lista de personas.'),
      complete: verificar,
    });
  }

  guardar(): void {
    if (this.guardando()) return;
    this.error.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valor = this.form.getRawValue();
    this.guardando.set(true);

    this.evaluacionService
      .iniciar({
        idPersona: valor.persona!.id,
        idPeriodoId: valor.periodo!.id,
      })
      .subscribe({
        next: (evaluacion) =>
          this.router.navigate(['/evaluador/nutricional', evaluacion.idEvaluacion]),
        error: (err: HttpErrorResponse) => {
          if (err.status === 409) {
            this.error.set(err.error?.message ?? 'La persona ya tiene evaluación en ese periodo.');
          } else if (err.status === 404) {
            this.error.set(err.error?.message ?? 'Persona o periodo no encontrado.');
          } else {
            this.error.set('No se pudo iniciar la evaluación.');
          }
          this.guardando.set(false);
        },
      });
  }

  cancelar(): void {
    this.router.navigate(['/evaluador/nutricional']);
  }
}
