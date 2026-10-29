import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TuiButton, TuiIcon, TuiLabel, TuiTextfield } from '@taiga-ui/core';
import { TuiBadge, TuiButtonLoading, TuiTextarea } from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { EvaluacionService } from './evaluacion-service';
import { EvaluacionResponse, EstadoEvaluacion } from './evaluacion.model';
import { AntropometricoService } from '../antropometrico/antropometrico-service';
import { BioquimicoService } from '../bioquimico/bioquimico-service';
import { ConfirmService } from '../../../../core/services/confirm-service';

@Component({
  selector: 'app-evaluacion-detail',
  imports: [RouterLink, FormsModule, TuiButton, TuiButtonLoading, TuiBadge, TuiIcon, TuiLabel, TuiTextfield, TuiTextarea, TuiTable],
  templateUrl: './evaluacion-detail.html',
  styleUrl: './evaluacion-detail.css',
})
export class EvaluacionDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly evaluacionService = inject(EvaluacionService);

  protected readonly evaluacion = signal<EvaluacionResponse | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly observaciones = signal('');
  protected readonly guardandoObs = signal(false);

  private readonly antropometricoService = inject(AntropometricoService);
  private readonly bioquimicoService = inject(BioquimicoService);
  private readonly confirmar = inject(ConfirmService);

  private idEvaluacion!: number;

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      this.error.set('ID de evaluación no válido.');
      return;
    }
    this.idEvaluacion = Number(idParam);
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);

    this.evaluacionService.obtener(this.idEvaluacion).subscribe({
      next: (data) => {
        this.evaluacion.set(data);
        this.observaciones.set(data.observaciones ?? '');
      },
      error: () => {
        this.error.set('No se pudo cargar la evaluación.');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  guardarObservaciones(): void {
    if (!this.evaluacion() || this.guardandoObs()) return;
    if (this.evaluacion()!.estadoEvaluacion === 'ANULADA') return;

    this.guardandoObs.set(true);
    this.error.set(null);

    this.evaluacionService.actualizarObservaciones(
      this.idEvaluacion,
      this.observaciones()
    ).subscribe({
      next: (data) => {
        this.evaluacion.set(data);
        this.guardandoObs.set(false);
      },
      error: () => {
        this.error.set('No se pudieron guardar las observaciones.');
        this.guardandoObs.set(false);
      },
    });
  }

  descargarPdf(): void {
    this.evaluacionService.descargarPdf(this.idEvaluacion).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `ficha-${this.idEvaluacion}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => this.error.set('No se pudo generar la ficha PDF.'),
    });
  }

  protected claseEstado(estado: EstadoEvaluacion): string {
    switch (estado) {
      case 'COMPLETA': return 'badge badge-completa';
      case 'ANULADA': return 'badge badge-anulada';
      default: return 'badge badge-pendiente';
    }
  }

  // -------------------------------------------------------------------
  // Interpretación del diagnóstico bioquímico
  // dxBioquimico llega como "NORMAL" o "ALTERADO: GLUCOSA, COLESTEROL, ..."
  // -------------------------------------------------------------------

  /** Analitos alterados extraídos del dx ("ALTERADO: GLUCOSA, …"). */
  protected readonly bioAfectados = computed<Set<string>>(() => {
    const dx = this.evaluacion()?.detalleBioquimico?.dxBioquimico ?? '';
    const m = /ALTERADO\s*:\s*(.+)/i.exec(dx);
    if (!m) return new Set<string>();
    return new Set(
      m[1]
        .split(',')
        .map((s) => s.trim().toUpperCase().replace(/[^A-Z_]/g, ''))
        .filter((s) => !!s)
    );
  });

  protected readonly bioAlterado = computed(() =>
    /ALTERADO/i.test(this.evaluacion()?.detalleBioquimico?.dxBioquimico ?? '')
  );

  protected readonly bioNombresAlterados = computed<string>(() =>
    Array.from(this.bioAfectados())
      .map((clave) => this.nombreAnalito(clave))
      .join(', ')
  );

  private nombreAnalito(clave: string): string {
    switch (clave) {
      case 'GLUCOSA': return 'Glucosa';
      case 'COLESTEROL': return 'Colesterol';
      case 'TRIGLICERIDOS': return 'Triglicéridos';
      case 'PRESION_SISTOLICA': return 'Presión sistólica';
      case 'PRESION_DIASTOLICA': return 'Presión diastólica';
      default: return clave;
    }
  }

  /** Estado de un analito: neutro sin dato, ok normal, danger alterado. */
  protected bioEstado(clave: string, valor: number | null | undefined): 'ok' | 'danger' | 'neutro' {
    if (valor === null || valor === undefined) return 'neutro';
    const dx = this.evaluacion()?.detalleBioquimico?.dxBioquimico ?? '';
    if (!dx) return 'neutro';
    if (!this.bioAlterado()) return 'ok';
    return this.bioAfectados().has(clave) ? 'danger' : 'ok';
  }

  /** Estado combinado de la presión arterial. */
  protected bioEstadoPresion(bio: {
    presionSistolica: number | null;
    presionDiastolica: number | null;
  }): 'ok' | 'danger' | 'neutro' {
    const estados = [
      this.bioEstado('PRESION_SISTOLICA', bio.presionSistolica),
      this.bioEstado('PRESION_DIASTOLICA', bio.presionDiastolica),
    ];
    if (estados.includes('danger')) return 'danger';
    if (estados.includes('ok')) return 'ok';
    return 'neutro';
  }

  protected bioClaseGlobal(): string {
    const dx = this.evaluacion()?.detalleBioquimico?.dxBioquimico;
    if (!dx) return 'pill-soft-neutral';
    return this.bioAlterado() ? 'pill-soft-error' : 'pill-soft-success';
  }

  protected bioEtiquetaGlobal(): string {
    const dx = this.evaluacion()?.detalleBioquimico?.dxBioquimico?.trim();
    if (!dx) return 'Sin datos';
    return this.bioAlterado() ? 'Alterado' : 'Normal';
  }

  protected bioClaseEstado(estado: 'ok' | 'danger' | 'neutro'): string {
    switch (estado) {
      case 'ok': return 'pill-soft-success';
      case 'danger': return 'pill-soft-error';
      default: return 'pill-soft-neutral';
    }
  }

  protected bioEtiquetaEstado(estado: 'ok' | 'danger' | 'neutro'): string {
    switch (estado) {
      case 'ok': return 'Normal';
      case 'danger': return 'Alterado';
      default: return 'Sin dato';
    }
  }

  recalcularAntropometria(): void {
    this.confirmar
      .confirmar('¿Recalcular los diagnósticos OMRON con los datos actuales de la persona?', 'Recalcular')
      .subscribe((ok) => {
        if (!ok) return;
        this.error.set(null);
        this.antropometricoService.recalcular(this.idEvaluacion).subscribe({
          next: () => this.cargar(),
          error: (err) => {
            if (err.status === 404) {
              this.error.set('No existe antropometría para recalcular.');
            } else {
              this.error.set('No se pudo recalcular la antropometría.');
            }
          },
        });
      });
  }
}
