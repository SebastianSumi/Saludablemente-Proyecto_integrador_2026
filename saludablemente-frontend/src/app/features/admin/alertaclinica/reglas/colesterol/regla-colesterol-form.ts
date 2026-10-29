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
import { ReglaColesterolService } from './regla-colesterol-service';
import {
  ClasificacionColesterol,
  ReglaColesterolRequest,
  SexoBiologico,
} from './regla-colesterol.model';

export interface OpcionCombo {
  id: string;
  label: string;
}

@Component({
  selector: 'app-regla-colesterol-form',
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
  templateUrl: './regla-colesterol-form.html',
  styleUrls: ['./regla-colesterol-form.css'],
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
export class ReglaColesterolForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ReglaColesterolService);
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
    { id: 'DESEABLE', label: 'Deseable (< 200 mg/dL)' },
    { id: 'LIMITE', label: 'Límite (200 - 239 mg/dL)' },
    { id: 'ALTO', label: 'Alto (≥ 240 mg/dL)' },
  ];

  protected readonly form = this.fb.group({
    sexo: [this.sexos[0] as OpcionCombo | null, Validators.required],
    clasificacion: [this.clasificaciones[0] as OpcionCombo | null, Validators.required],
    edadMin: [0, [Validators.required, Validators.min(0)]],
    edadMax: [100, [Validators.required, Validators.max(120)]],
    colesterolMgDlMin: [0, [Validators.required, Validators.min(0)]],
    colesterolMgDlMax: [1000, [Validators.required, Validators.max(1000)]],
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
          colesterolMgDlMin: regla.colesterolMgDlMin,
          colesterolMgDlMax: regla.colesterolMgDlMax,
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
    const payload: ReglaColesterolRequest = {
      sexo: (valor.sexo?.id ?? 'M') as SexoBiologico,
      clasificacion: (valor.clasificacion?.id ?? 'DESEABLE') as ClasificacionColesterol,
      edadMin: Number(valor.edadMin),
      edadMax: Number(valor.edadMax),
      colesterolMgDlMin: Number(valor.colesterolMgDlMin),
      colesterolMgDlMax: Number(valor.colesterolMgDlMax),
      puntosRiesgo: Number(valor.puntosRiesgo),
    };

    const id = this.id();
    const peticion = id
      ? this.service.actualizar(id, payload)
      : this.service.crear(payload);

    this.guardando.set(true);
    peticion.subscribe({
      next: () => this.router.navigate(['/alertaclinica/reglas/colesterol']),
      error: (err: HttpErrorResponse) => {
        this.error.set(this.mapearError(err));
        this.guardando.set(false);
      },
    });
  }

  cancelar(): void {
    this.router.navigate(['/alertaclinica/reglas/colesterol']);
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
