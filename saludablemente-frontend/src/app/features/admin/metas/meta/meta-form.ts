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
import {
  TuiButtonLoading,
  TuiChevron,
  TuiDataListWrapper,
  TuiComboBox,
  TuiInputDate,
  TuiTabs,
} from '@taiga-ui/kit';
import { fijarHoyEn, limpiarFechaEn } from '../../../../core/forms/fecha-transformers';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MetaService } from '../meta-service';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { PersonaService } from '../../personal/persona/persona-service';
import { ModalidadMeta, PilarBienestar, TipoCalculo } from '../models';
import { Persona } from '../../personal/persona/persona.model';

@Component({
  selector: 'app-meta-form',
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
    TuiTabs,
    TuiTextfield,
  ],
  templateUrl: './meta-form.html',
  styleUrl: './meta-form.css',
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal(
        (item: unknown): string => {
          const o = item as { nombre?: string } | null;
          return o?.nombre ?? (typeof item === 'string' ? (item as string) : '');
        }
      ),
      identityMatcher: signal(
        (a: unknown, b: unknown): boolean =>
          ((a as { id?: unknown } | null)?.id ?? a ?? null) ===
          ((b as { id?: unknown } | null)?.id ?? b ?? null)
      ),
    }),
  ],
})
export class MetaForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(MetaService);
  private readonly personasSrv = inject(PersonaService);
  private readonly confirmar = inject(ConfirmService);
  private readonly router = inject(Router);

  protected readonly tabActivo = signal(0);
  protected readonly personas = signal<Persona[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);
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

  protected readonly form = this.fb.nonNullable.group({
    persona: [null as Persona | null, [Validators.required]],
    pilar: ['', [Validators.required]],
    modalidad: ['CUANTITATIVA', [Validators.required]],
    tipoCalculo: ['ACUMULADO', [Validators.required]],
    descripcion: ['', [Validators.required, Validators.maxLength(200)]],
    indicador: ['', [Validators.required, Validators.maxLength(60)]],
    unidad: ['', [Validators.maxLength(20)]],
    valorInicial: [0, [Validators.required, Validators.min(0)]],
    valorObjetivo: [1, [Validators.required, Validators.min(0.01)]],
    fechaInicio: ['', [Validators.required]],
    fechaObjetivo: ['', [Validators.required]],
    idEvaluacionOrigen: [''],
    idAlertaOrigen: [''],
    idProfesionalPrescriptor: [''],
    tipoMeta: ['', [Validators.maxLength(60)]],
    fechaLimite: [''],
  });

  ngOnInit(): void {
    this.loading.set(true);
    this.personasSrv.listar({ estado: true }).pipe(catchError(() => of([]))).subscribe((p) => {
      this.personas.set(p);
      this.loading.set(false);
    });
  }

  protected nombrePersona(p: Persona): string {
    return [p.apellidoPaterno, p.apellidoMaterno, p.nombres].filter((x) => !!x).join(' ');
  }

  guardar(): void {
    const modo = this.tabActivo();
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Completa los campos obligatorios.');
      return;
    }
    const v = this.form.getRawValue();
    const persona = v.persona as Persona | null;
    if (!persona) {
      this.error.set('Selecciona la persona.');
      return;
    }
    if (modo !== 2 && v.fechaInicio >= v.fechaObjetivo) {
      this.error.set('La fecha de inicio debe ser anterior a la fecha objetivo.');
      return;
    }
    if (modo === 1 && (!v.idEvaluacionOrigen || !v.idAlertaOrigen || !v.idProfesionalPrescriptor)) {
      this.error.set('La meta clínica requiere evaluación, alerta y profesional.');
      return;
    }

    const base = {
      idPersona: persona.id,
      pilar: v.pilar as PilarBienestar,
      descripcion: (v.descripcion ?? '').trim(),
      valorInicial: Number(v.valorInicial),
      valorObjetivo: Number(v.valorObjetivo),
    };
    const peticion =
      modo === 0
        ? this.service.crearPersonal({
            ...base,
            modalidad: v.modalidad as ModalidadMeta,
            tipoCalculo: v.tipoCalculo as TipoCalculo,
            indicador: (v.indicador ?? '').trim(),
            unidad: (v.unidad ?? '').trim() || undefined,
            fechaInicio: v.fechaInicio,
            fechaObjetivo: v.fechaObjetivo,
          })
        : modo === 1
          ? this.service.crearClinica({
              ...base,
              modalidad: v.modalidad as ModalidadMeta,
              tipoCalculo: v.tipoCalculo as TipoCalculo,
              indicador: (v.indicador ?? '').trim(),
              unidad: (v.unidad ?? '').trim() || undefined,
              fechaInicio: v.fechaInicio,
              fechaObjetivo: v.fechaObjetivo,
              idEvaluacionOrigen: Number(v.idEvaluacionOrigen),
              idAlertaOrigen: Number(v.idAlertaOrigen),
              idProfesionalPrescriptor: Number(v.idProfesionalPrescriptor),
            })
          : this.service.crearSugerida({
              idPersona: persona.id,
              tipoMeta: (v.tipoMeta ?? '').trim(),
              descripcion: (v.descripcion ?? '').trim(),
              valorInicial: Number(v.valorInicial),
              valorObjetivo: Number(v.valorObjetivo),
              fechaLimite: v.fechaLimite,
              pilar: v.pilar as PilarBienestar,
            });

    this.confirmar.confirmar('¿Crear esta meta?', 'Crear').subscribe((ok) => {
      if (!ok) return;
      this.guardando.set(true);
      this.error.set(null);
      peticion.subscribe({
        next: (m) => this.router.navigate(['/metas', m.id]),
        error: (err: HttpErrorResponse) => {
          this.error.set(err.error?.message ?? 'No se pudo crear la meta.');
          this.guardando.set(false);
        },
      });
    });
  }

  cancelar(): void {
    this.router.navigate(['/metas']);
  }

  protected fijarHoy(campo: string): void {
    fijarHoyEn(this.form.get(campo));
  }

  protected limpiarCampo(campo: string): void {
    limpiarFechaEn(this.form.get(campo));
  }
}
