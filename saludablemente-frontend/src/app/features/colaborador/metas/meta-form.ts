import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
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
  TuiTitle,
} from '@taiga-ui/core';
import {
  TuiAvatar,
  TuiButtonLoading,
  TuiChevron,
  TuiComboBox,
  TuiDataListWrapper,
  TuiInputDate,
} from '@taiga-ui/kit';
import { TuiCardLarge, TuiHeader } from '@taiga-ui/layout';
import { fijarHoyEn, limpiarFechaEn } from '../../../core/forms/fecha-transformers';
import { PerfilService } from '../../admin/perfil/perfil-service';
import { MetaService } from '../../admin/metas/meta-service';
import { ConfirmService } from '../../../core/services/confirm-service';
import type { ModalidadMeta, PilarBienestar, TipoCalculo } from '../../admin/metas/models';

@Component({
  selector: 'app-col-meta-form',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    TuiAvatar,
    TuiButton,
    TuiButtonLoading,
    TuiCalendar,
    TuiCardLarge,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown,
    TuiError,
    TuiFilterByInputPipe,
    TuiHeader,
    TuiIcon,
    TuiInput,
    TuiInputDate,
    TuiLabel,
    TuiTextfield,
    TuiTitle,
  ],
  templateUrl: './meta-form.html',
  styleUrls: ['./meta-form.css'],
})
export class ColMetaForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly perfilSrv = inject(PerfilService);
  private readonly service = inject(MetaService);
  private readonly confirmar = inject(ConfirmService);
  private readonly router = inject(Router);

  protected readonly idPersona = signal<number | null>(null);
  protected readonly nombreCompleto = signal('');
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(true);
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
    pilar: ['', [Validators.required]],
    modalidad: ['CUANTITATIVA' as ModalidadMeta, [Validators.required]],
    tipoCalculo: ['ACUMULADO' as TipoCalculo, [Validators.required]],
    descripcion: ['', [Validators.required, Validators.maxLength(200)]],
    indicador: ['', [Validators.required, Validators.maxLength(60)]],
    unidad: ['', [Validators.maxLength(20)]],
    valorInicial: [0, [Validators.required, Validators.min(0)]],
    valorObjetivo: [1, [Validators.required, Validators.min(0.01)]],
    fechaInicio: ['', [Validators.required]],
    fechaObjetivo: ['', [Validators.required]],
  });

  ngOnInit(): void {
    this.perfilSrv.obtener().subscribe({
      next: (p) => {
        if (!p.idPersona) {
          this.error.set('No se pudo resolver tu perfil de persona.');
          this.loading.set(false);
          return;
        }
        this.idPersona.set(p.idPersona);
        this.nombreCompleto.set(p.nombreCompleto);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 0
            ? 'No se pudo conectar con el servidor.'
            : 'No se pudo resolver tu perfil.',
        );
        this.loading.set(false);
      },
    });
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Completa los campos obligatorios.');
      return;
    }
    const idPersona = this.idPersona();
    if (!idPersona) {
      this.error.set('No se pudo resolver tu perfil de persona.');
      return;
    }
    const v = this.form.getRawValue();
    if (v.fechaInicio >= v.fechaObjetivo) {
      this.error.set('La fecha de inicio debe ser anterior a la fecha objetivo.');
      return;
    }
    const request = {
      idPersona,
      pilar: v.pilar as PilarBienestar,
      modalidad: v.modalidad,
      tipoCalculo: v.tipoCalculo,
      descripcion: (v.descripcion ?? '').trim(),
      indicador: (v.indicador ?? '').trim(),
      unidad: (v.unidad ?? '').trim() || undefined,
      valorInicial: Number(v.valorInicial),
      valorObjetivo: Number(v.valorObjetivo),
      fechaInicio: v.fechaInicio,
      fechaObjetivo: v.fechaObjetivo,
    };
    this.confirmar.confirmar('¿Crear esta meta personal?', 'Crear').subscribe((ok) => {
      if (!ok) return;
      this.guardando.set(true);
      this.error.set(null);
      this.service.crearPersonal(request).subscribe({
        next: (m) => this.router.navigate(['/colaborador/mis-metas', m.id]),
        error: (err: HttpErrorResponse) => {
          this.error.set(err.error?.message ?? 'No se pudo crear la meta.');
          this.guardando.set(false);
        },
      });
    });
  }

  cancelar(): void {
    this.router.navigate(['/colaborador/mis-metas']);
  }

  protected fijarHoy(campo: string): void {
    fijarHoyEn(this.form.get(campo));
  }

  protected limpiarCampo(campo: string): void {
    limpiarFechaEn(this.form.get(campo));
  }
}
