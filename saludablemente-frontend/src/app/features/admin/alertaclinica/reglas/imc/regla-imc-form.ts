import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import {
  TuiButton,
  TuiDropdown,
  TuiFilterByInputPipe,
  TuiError,
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
import { ReglaImcService } from './regla-imc-service';
import {
  ClasificacionImc,
  ReglaImcRequest,
  SexoBiologico,
} from './regla-imc.model';

export interface OpcionCombo {
  id: string;
  label: string;
}

@Component({
  selector: 'app-regla-imc-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    TuiButton,
    TuiButtonLoading,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown,
    TuiFilterByInputPipe,
    TuiError,
    TuiIcon,
    TuiInput,
    TuiLabel,
    TuiTextfield,
  ],
  templateUrl: './regla-imc-form.html',
  styleUrls: ['./regla-imc-form.css'],
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal((item: unknown): string => {
        if (item === null || item === undefined) return '';
        if (typeof item === 'string') return item;
        const o = item as { label?: string; nombre?: string };
        return o.label ?? o.nombre ?? '';
      }),
      identityMatcher: signal((a: unknown, b: unknown): boolean => {
        if (a === b) return true;
        if (a === null || a === undefined || b === null || b === undefined) return false;
        if (typeof a === 'string' || typeof b === 'string') return a === b;
        const x = a as { id?: unknown; label?: string };
        const y = b as { id?: unknown; label?: string };
        return (x.id ?? x.label) === (y.id ?? y.label);
      }),
    }),
  ],
})
export class ReglaImcForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ReglaImcService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly id = signal<number | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly guardando = signal(false);
  protected readonly errorCarga = signal(false);

  readonly sexos: OpcionCombo[] = [
    { id: 'M', label: 'Masculino' },
    { id: 'F', label: 'Femenino' },
  ];

  readonly clasificaciones: OpcionCombo[] = [
    { id: 'DELGADEZ', label: 'Delgadez (< 18.5 kg/m²)' },
    { id: 'NORMAL', label: 'Normal (18.5 - 24.9 kg/m²)' },
    { id: 'SOBREPESO', label: 'Sobrepeso (25.0 - 29.9 kg/m²)' },
    { id: 'OBESIDAD', label: 'Obesidad (≥ 30.0 kg/m²)' },
  ];

  protected readonly form = this.fb.group({
    sexo: [this.sexos[0] as OpcionCombo | null, Validators.required],
    clasificacion: [this.clasificaciones[0] as OpcionCombo | null, Validators.required],
    edadMin: [0, [Validators.required, Validators.min(0)]],
    edadMax: [100, [Validators.required, Validators.max(120)]],
    imcMin: [0, [Validators.required, Validators.min(0)]],
    imcMax: [100, [Validators.required, Validators.max(100)]],
    puntosRiesgo: [0, [Validators.required, Validators.min(0)]],
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.id.set(id);
      this.cargarRegla(id);
    }
  }

  private cargarRegla(id: number): void {
    this.loading.set(true);
    this.errorCarga.set(false);
    this.error.set(null);

    this.service.obtener(id).subscribe({
      next: (regla) => {
        this.form.patchValue({
          sexo: this.sexos.find((s) => s.id === regla.sexo) ?? null,
          clasificacion: this.clasificaciones.find((c) => c.id === regla.clasificacion) ?? null,
          edadMin: regla.edadMin,
          edadMax: regla.edadMax,
          imcMin: regla.imcMin,
          imcMax: regla.imcMax,
          puntosRiesgo: regla.puntosRiesgo,
        });
        this.loading.set(false);
      },
      error: () => {
        this.errorCarga.set(true);
        this.error.set('No se pudo cargar la información de la regla clínica.');
        this.loading.set(false);
      },
    });
  }

  guardar(): void {
    if (this.loading() || this.guardando() || this.errorCarga()) return;
    this.error.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valor = this.form.getRawValue();
    const payload: ReglaImcRequest = {
      sexo: (valor.sexo?.id ?? 'M') as SexoBiologico,
      clasificacion: (valor.clasificacion?.id ?? 'NORMAL') as ClasificacionImc,
      edadMin: Number(valor.edadMin),
      edadMax: Number(valor.edadMax),
      imcMin: Number(valor.imcMin),
      imcMax: Number(valor.imcMax),
      puntosRiesgo: Number(valor.puntosRiesgo),
    };

    const id = this.id();
    const peticion = id
      ? this.service.actualizar(id, payload)
      : this.service.crear(payload);

    this.guardando.set(true);
    peticion.subscribe({
      next: () => this.router.navigate(['/alertaclinica/reglas/imc']),
      error: (err: HttpErrorResponse) => {
        this.error.set(this.mapearError(err));
        this.guardando.set(false);
      },
    });
  }

  cancelar(): void {
    this.router.navigate(['/alertaclinica/reglas/imc']);
  }

  private mapearError(err: HttpErrorResponse): string {
    if (err.status === 0) return 'No se pudo conectar con el servidor.';
    if (err.status === 404) return err.error?.message ?? 'Regla no encontrada.';
    if (err.status === 409) return err.error?.message ?? 'Existe una regla que se solapa con este rango.';
    if (err.status === 400) return err.error?.message ?? 'Datos inválidos en el formulario.';
    if (err.status === 500) return 'Error interno del servidor.';
    return err.error?.message ?? 'Operación fallida.';
  }
}
