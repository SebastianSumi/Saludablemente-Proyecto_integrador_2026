import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  TuiButton,
  TuiError,
  TuiIcon,
  TuiInput,
  TuiLabel,
  TuiTextfield,
} from '@taiga-ui/core';
import { TuiButtonLoading, TuiTabs, TuiTextarea } from '@taiga-ui/kit';
import { EvaluacionService } from '../../admin/nutricional/evaluacion/evaluacion-service';
import { AntropometricoService } from '../../admin/nutricional/antropometrico/antropometrico-service';
import { BioquimicoService } from '../../admin/nutricional/bioquimico/bioquimico-service';
import { EvaluacionResponse } from '../../admin/nutricional/evaluacion/evaluacion.model';
import { AntropometricoRequest } from '../../admin/nutricional/antropometrico/antropometrico.model';
import { BioquimicoManualRequest } from '../../admin/nutricional/bioquimico/bioquimico.model';

@Component({
  selector: 'app-eval-nutricional-detalle',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    ReactiveFormsModule,
    TuiButton,
    TuiButtonLoading,
    TuiError,
    TuiIcon,
    TuiInput,
    TuiLabel,
    TuiTabs,
    TuiTextarea,
    TuiTextfield,
  ],
  templateUrl: './eval-nutricional-detalle.html',
  styleUrls: ['../evaluador-comun.css', '../evaluador-tablas-detalle.css'],
})
export class EvalNutricionalDetalle implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly evaluacionSrv = inject(EvaluacionService);
  private readonly antroSrv = inject(AntropometricoService);
  private readonly bioSrv = inject(BioquimicoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly tabActivo = signal(0);
  protected readonly evaluacion = signal<EvaluacionResponse | null>(null);
  protected readonly loading = signal(false);
  protected readonly guardando = signal(false);
  protected readonly editandoObs = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly mensajeExito = signal<string | null>(null);
  protected readonly modoEdicionAntro = signal(false);
  protected readonly modoEdicionBio = signal(false);

  protected readonly antroForm = this.fb.nonNullable.group({
    estaturaCm: [0, [Validators.required, Validators.min(100), Validators.max(250)]],
    pesoKg: [0, [Validators.required, Validators.min(20), Validators.max(300)]],
    perimetroAbdominalCm: [null as number | null, [Validators.min(40), Validators.max(200)]],
    porcentajeGrasa: [null as number | null, [Validators.min(0), Validators.max(70)]],
    porcentajeMasaMuscular: [null as number | null, [Validators.min(0), Validators.max(100)]],
    porcentajeGrasaVisceral: [null as number | null, [Validators.min(0), Validators.max(30)]],
  });

  protected readonly bioForm = this.fb.group({
    glucosa: [null as number | null],
    colesterol: [null as number | null],
    trigliceridos: [null as number | null],
    presionSistolica: [null as number | null],
    presionDiastolica: [null as number | null],
  });

  protected readonly obsForm = this.fb.nonNullable.group({
    observaciones: ['', [Validators.maxLength(500)]],
  });

  private id = 0;

  ngOnInit(): void {
    const idParam = Number(this.route.snapshot.paramMap.get('id'));
    if (!idParam) {
      this.router.navigate(['/evaluador/nutricional']);
      return;
    }
    this.id = idParam;
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);

    this.evaluacionSrv.obtener(this.id).subscribe({
      next: (e) => {
        this.evaluacion.set(e);

        const antro = e.detalleAntropometrico;
        if (antro) {
          this.modoEdicionAntro.set(true);
          this.antroForm.patchValue({
            estaturaCm: antro.estaturaCm,
            pesoKg: antro.pesoKg,
            perimetroAbdominalCm: antro.perimetroAbdominalCm ?? null,
            porcentajeGrasa: antro.porcentajeGrasa ?? null,
            porcentajeMasaMuscular: antro.porcentajeMasaMuscular ?? null,
            porcentajeGrasaVisceral: antro.porcentajeGrasaVisceral ?? null,
          });
        }

        const bio = e.detalleBioquimico;
        if (bio) {
          this.modoEdicionBio.set(true);
          this.bioForm.patchValue({
            glucosa: bio.glucosa,
            colesterol: bio.colesterol,
            trigliceridos: bio.trigliceridos,
            presionSistolica: bio.presionSistolica,
            presionDiastolica: bio.presionDiastolica,
          });
        }

        this.obsForm.patchValue({ observaciones: e.observaciones ?? '' });
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 404 ? 'Evaluación no encontrada.' : 'No se pudo cargar la evaluación.'
        );
        this.loading.set(false);
      },
    });
  }

  guardarAntropometria(): void {
    if (this.antroForm.invalid || this.guardando()) {
      this.antroForm.markAllAsTouched();
      return;
    }
    const v = this.antroForm.getRawValue();
    const request: AntropometricoRequest = {
      estaturaCm: v.estaturaCm,
      pesoKg: v.pesoKg,
      perimetroAbdominalCm: v.perimetroAbdominalCm ?? undefined,
      porcentajeGrasa: v.porcentajeGrasa ?? undefined,
      porcentajeMasaMuscular: v.porcentajeMasaMuscular ?? undefined,
      porcentajeGrasaVisceral: v.porcentajeGrasaVisceral ?? undefined,
    };
    this.guardando.set(true);
    this.error.set(null);
    this.mensajeExito.set(null);

    const peticion = this.modoEdicionAntro()
      ? this.antroSrv.actualizar(this.id, request)
      : this.antroSrv.registrar(this.id, request);

    peticion.subscribe({
      next: () => {
        this.mensajeExito.set('Antropometría guardada correctamente.');
        this.guardando.set(false);
        this.cargar();
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(err.error?.message ?? 'No se pudo guardar la antropometría.');
        this.guardando.set(false);
      },
    });
  }

  recalcular(): void {
    if (this.guardando()) return;
    this.guardando.set(true);
    this.error.set(null);
    this.mensajeExito.set(null);
    this.antroSrv.recalcular(this.id).subscribe({
      next: () => {
        this.mensajeExito.set('Diagnósticos recalculados.');
        this.guardando.set(false);
        this.cargar();
      },
      error: () => {
        this.error.set('No se pudieron recalcular los diagnósticos.');
        this.guardando.set(false);
      },
    });
  }

  guardarBioquimico(): void {
    if (this.guardando()) return;
    const v = this.bioForm.getRawValue();
    const request: BioquimicoManualRequest = {
      glucosa: v.glucosa,
      colesterol: v.colesterol,
      trigliceridos: v.trigliceridos,
      presionSistolica: v.presionSistolica,
      presionDiastolica: v.presionDiastolica,
    };
    this.guardando.set(true);
    this.error.set(null);
    this.mensajeExito.set(null);

    const peticion = this.modoEdicionBio()
      ? this.bioSrv.actualizarManual(this.id, request)
      : this.bioSrv.registrarManual(this.id, request);

    peticion.subscribe({
      next: () => {
        this.mensajeExito.set('Bioquímica guardada correctamente.');
        this.guardando.set(false);
        this.cargar();
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(err.error?.message ?? 'No se pudo guardar la bioquímica.');
        this.guardando.set(false);
      },
    });
  }

  guardarObservaciones(): void {
    const texto = (this.obsForm.controls.observaciones.value ?? '').trim();
    if (this.guardando()) return;
    this.guardando.set(true);
    this.error.set(null);
    this.mensajeExito.set(null);
    this.evaluacionSrv.actualizarObservaciones(this.id, texto).subscribe({
      next: () => {
        this.mensajeExito.set('Observaciones actualizadas.');
        this.editandoObs.set(false);
        this.guardando.set(false);
        this.cargar();
      },
      error: () => {
        this.error.set('No se pudieron guardar las observaciones.');
        this.guardando.set(false);
      },
    });
  }

  descargarPdf(): void {
    this.evaluacionSrv.descargarPdf(this.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `ficha-${this.id}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => this.error.set('No se pudo generar la ficha PDF.'),
    });
  }
}
