import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TuiButton, TuiIcon, TuiInput, TuiLabel, TuiTextfield } from '@taiga-ui/core';
import { TuiButtonLoading } from '@taiga-ui/kit';
import { EvaluacionService } from '../../admin/nutricional/evaluacion/evaluacion-service';
import { AlertaClinicaService } from '../../admin/alertaclinica/alerta/alerta-clinica-service';
import { AlertaAgregado } from '../../admin/alertaclinica/alerta/alerta-clinica.model';

interface OpcionPeriodo {
  id: number;
  codigo: string;
  label: string;
}

@Component({
  selector: 'app-eval-reportes',
  standalone: true,
  imports: [FormsModule, TuiButton, TuiButtonLoading, TuiIcon, TuiInput, TuiLabel, TuiTextfield],
  templateUrl: './eval-reportes.html',
  styleUrls: ['../evaluador-comun.css', '../evaluador-tablas-detalle.css'],
})
export class EvalReportes implements OnInit {
  private readonly evaluacionSrv = inject(EvaluacionService);
  private readonly alertaSrv = inject(AlertaClinicaService);

  protected readonly periodos = signal<OpcionPeriodo[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly mensaje = signal<string | null>(null);
  protected readonly descargando = signal(false);

  protected idEvaluacion = '';
  protected periodoCodigo = '';

  protected readonly alertaAgregado = signal<AlertaAgregado | null>(null);
  protected alertaDesde = '';
  protected alertaHasta = '';

  ngOnInit(): void {
    this.evaluacionSrv.periodosDisponibles().subscribe({
      next: (data) =>
        this.periodos.set(
          data
            .filter((p) => p.id !== undefined)
            .map((p) => ({ id: p.id!, codigo: p.codigo, label: `${p.codigo} — ${p.descripcion}` }))
        ),
      error: () => this.error.set('No se pudieron cargar los periodos disponibles.'),
    });
    this.cargarResumenAlertas();
  }

  descargarFicha(): void {
    const id = Number(this.idEvaluacion);
    if (!id || id <= 0) {
      this.error.set('Ingresa un ID de evaluación válido.');
      return;
    }
    this.error.set(null);
    this.mensaje.set(null);
    this.descargando.set(true);
    this.evaluacionSrv.descargarPdf(id).subscribe({
      next: (blob) => {
        this.guardar(blob, `ficha-nutricional-${id}.pdf`);
        this.mensaje.set('Ficha clínica generada.');
        this.descargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo generar la ficha PDF para ese ID.');
        this.descargando.set(false);
      },
    });
  }

  exportarConsolidado(): void {
    if (!this.periodoCodigo) {
      this.error.set('Selecciona un periodo para exportar el consolidado.');
      return;
    }
    this.error.set(null);
    this.mensaje.set(null);
    this.descargando.set(true);
    this.evaluacionSrv.exportarConsolidado(this.periodoCodigo).subscribe({
      next: (blob) => {
        this.guardar(blob, `consolidado-nutricional-${this.periodoCodigo}.xlsx`);
        this.mensaje.set('Consolidado exportado.');
        this.descargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo generar el consolidado.');
        this.descargando.set(false);
      },
    });
  }

  cargarResumenAlertas(): void {
    this.alertaSrv.reporte(this.alertaDesde || undefined, this.alertaHasta || undefined).subscribe({
      next: (r) => this.alertaAgregado.set(r.agregado),
      error: () => undefined,
    });
  }

  private guardar(blob: Blob, nombre: string): void {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = nombre;
    a.click();
    window.URL.revokeObjectURL(url);
  }
}
