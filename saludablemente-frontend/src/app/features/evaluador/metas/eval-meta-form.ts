import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, of } from 'rxjs';
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
import { MetaService } from '../../admin/metas/meta-service';
import { PersonaService } from '../../admin/personal/persona/persona-service';
import { PerfilService } from '../../admin/perfil/perfil-service';
import { ModalidadMeta, PilarBienestar, TipoCalculo } from '../../admin/metas/models';
import { Persona } from '../../admin/personal/persona/persona.model';

@Component({
  selector: 'app-eval-meta-form',
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
  templateUrl: './eval-meta-form.html',
  styleUrls: ['../evaluador-comun.css'],
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal(
        (item: unknown): string => {
          const o = item as
            | { label?: string; nombres?: string; documentoIdentidad?: string; apellidoPaterno?: string }
            | null;
          if (o?.nombres) {
            return `${o.documentoIdentidad ?? ''} ${o.apellidoPaterno ?? ''} ${o.nombres}`.trim();
          }
          return o?.label ?? '';
        }
      ),
      identityMatcher: signal(
        (a: unknown, b: unknown): boolean =>
          ((a as { id?: unknown } | null)?.id ?? null) ===
          ((b as { id?: unknown } | null)?.id ?? null)
      ),
    }),
  ],
})
export class EvalMetaForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly metaSrv = inject(MetaService);
  private readonly personasSrv = inject(PersonaService);
  private readonly perfilSrv = inject(PerfilService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly personas = signal<Persona[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly cargandoDatos = signal(true);
  protected readonly guardando = signal(false);

  protected readonly pilares: PilarBienestar[] = [
    'ELECCION',
    'DESCANSO',
    'MEDIO_AMBIENTE',
    'ACTIVIDAD',
    'CONFIANZA',
    'RELACIONES_INTERPERSONALES',
    'PERSPECTIVA',
    'NUTRICION',
  ];
  protected readonly modalidades: ModalidadMeta[] = ['CUANTITATIVA', 'CUALITATIVA'];
  protected readonly tiposCalculo: TipoCalculo[] = ['ULTIMO_VALOR', 'ACUMULADO'];

  private idEvaluacionOrigen: number | null = null;
  private idAlertaOrigen: number | null = null;

  protected readonly form = this.fb.nonNullable.group({
    persona: [null as Persona | null, [Validators.required]],
    pilar: ['NUTRICION' as PilarBienestar, [Validators.required]],
    modalidad: ['CUANTITATIVA' as ModalidadMeta, [Validators.required]],
    tipoCalculo: ['ULTIMO_VALOR' as TipoCalculo, [Validators.required]],
    indicador: ['', [Validators.required, Validators.maxLength(60)]],
    descripcion: ['', [Validators.required, Validators.maxLength(200)]],
    unidad: ['', [Validators.maxLength(20)]],
    valorInicial: [0, [Validators.required, Validators.min(0)]],
    valorObjetivo: [1, [Validators.required, Validators.min(0.01)]],
    fechaInicio: [new Date().toISOString().slice(0, 10), [Validators.required]],
    fechaObjetivo: ['', [Validators.required]],
  });

  ngOnInit(): void {
    const personaParam = Number(this.route.snapshot.queryParamMap.get('persona')) || null;
    const evalParam = Number(this.route.snapshot.queryParamMap.get('evaluacion')) || null;
    const alertaParam = Number(this.route.snapshot.queryParamMap.get('alerta')) || null;
    this.idEvaluacionOrigen = evalParam;
    this.idAlertaOrigen = alertaParam;

    this.personasSrv
      .listar({ estado: true })
      .pipe(catchError(() => of([])))
      .subscribe({
        next: (p) => {
          this.personas.set(p);
          if (personaParam) {
            const elegida = p.find((x) => x.id === personaParam);
            if (elegida) this.form.patchValue({ persona: elegida });
          }
          this.cargandoDatos.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar la lista de colaboradores.');
          this.cargandoDatos.set(false);
        },
      });
  }

  protected nombrePersona(p: Persona): string {
    return [p.apellidoPaterno, p.apellidoMaterno, p.nombres].filter((x) => !!x).join(' ');
  }

  guardar(): void {
    if (this.guardando() || this.cargandoDatos()) return;
    this.error.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Completa los campos obligatorios.');
      return;
    }

    const v = this.form.getRawValue();
    if (!v.persona) {
      this.error.set('Selecciona un colaborador.');
      return;
    }
    if (v.fechaInicio >= v.fechaObjetivo) {
      this.error.set('La fecha de inicio debe ser anterior a la fecha objetivo.');
      return;
    }

    const base = {
      idPersona: v.persona.id,
      pilar: v.pilar,
      modalidad: v.modalidad,
      tipoCalculo: v.tipoCalculo,
      indicador: v.indicador.trim(),
      descripcion: v.descripcion.trim(),
      unidad: v.unidad.trim() || undefined,
      valorInicial: Number(v.valorInicial),
      valorObjetivo: Number(v.valorObjetivo),
      fechaInicio: v.fechaInicio,
      fechaObjetivo: v.fechaObjetivo,
    };

    this.guardando.set(true);

    // Con origen clínico (evaluación + alerta) prescribimos meta clínica;
    // si no, registramos una meta personal para el colaborador.
    if (this.idEvaluacionOrigen && this.idAlertaOrigen) {
      this.perfilSrv.obtener().subscribe({
        next: (perfil) => {
          this.metaSrv
            .crearClinica({
              ...base,
              idEvaluacionOrigen: this.idEvaluacionOrigen!,
              idAlertaOrigen: this.idAlertaOrigen!,
              idProfesionalPrescriptor: perfil.idUsuario,
            })
            .subscribe({
              next: (m) => this.router.navigate(['/evaluador/metas', m.id]),
              error: (err: HttpErrorResponse) => this.errorGuardar(err),
            });
        },
        error: () => this.errorGuardar(),
      });
      return;
    }

    this.metaSrv.crearPersonal(base).subscribe({
      next: (m) => this.router.navigate(['/evaluador/metas', m.id]),
      error: (err: HttpErrorResponse) => this.errorGuardar(err),
    });
  }

  private errorGuardar(err?: HttpErrorResponse): void {
    this.error.set(err?.error?.message ?? 'No se pudo crear la meta.');
    this.guardando.set(false);
  }

  cancelar(): void {
    this.router.navigate(['/evaluador/metas']);
  }

  protected fijarHoy(campo: string): void {
    fijarHoyEn(this.form.get(campo));
  }

  protected limpiarCampo(campo: string): void {
    limpiarFechaEn(this.form.get(campo));
  }
}
