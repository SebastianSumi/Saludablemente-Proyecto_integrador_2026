import { downloadBlob } from '../../core/services/mobile-download';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TuiButton, TuiTextfield } from '@taiga-ui/core';
import { ReportesService } from './reportes-service';

@Component({
  selector: 'app-reportes',
  imports: [FormsModule, TuiButton, TuiTextfield],
  templateUrl: './reportes.html',
  styleUrl: './reportes.css',
})
export class Reportes {
  private readonly service = inject(ReportesService);

  protected readonly periodo = signal('');
  protected readonly desde = signal('');
  protected readonly hasta = signal('');
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  descargarAsistencias(): void {
    if (!this.periodo().trim()) {
      this.error.set('Indica un periodo (ej. 2026-1).');
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.service.asistencias(this.periodo().trim()).subscribe({
      next: (blob) => {
        void downloadBlob(blob, `asistencias-${this.periodo().trim()}.pdf`).catch(() => this.error.set('No se pudo abrir o guardar el documento.'));
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo generar el reporte.');
        this.loading.set(false);
      },
    });
  }

  descargarDatosPersonales(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.datosPersonales().subscribe({
      next: (blob) => {
        void downloadBlob(blob, 'datos-personales.pdf').catch(() => this.error.set('No se pudo abrir o guardar el documento.'));
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo generar el reporte.');
        this.loading.set(false);
      },
    });
  }

  descargarActividades(): void {
    if (!this.desde().trim() || !this.hasta().trim()) {
      this.error.set('Indica fechas desde/hasta (YYYY-MM-DD).');
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.service.actividades(this.desde().trim(), this.hasta().trim()).subscribe({
      next: (blob) => {
        void downloadBlob(blob, `actividades-${this.desde()}-${this.hasta()}.pdf`).catch(() => this.error.set('No se pudo abrir o guardar el documento.'));
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo generar el reporte.');
        this.loading.set(false);
      },
    });
  }

  descargarEvaluaciones(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.evaluaciones().subscribe({
      next: (blob) => {
        void downloadBlob(blob, 'evaluaciones.pdf').catch(() => this.error.set('No se pudo abrir o guardar el documento.'));
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo generar el reporte.');
        this.loading.set(false);
      },
    });
  }

  descargarAlertas(): void {
    if (!this.desde().trim() || !this.hasta().trim()) {
      this.error.set('Indica fechas desde/hasta (ISO, ej. 2026-01-01T00:00:00).');
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.service.alertas(this.desde().trim(), this.hasta().trim()).subscribe({
      next: (blob) => {
        void downloadBlob(blob, `alertas-${this.desde()}-${this.hasta()}.pdf`).catch(() => this.error.set('No se pudo abrir o guardar el documento.'));
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo generar el reporte.');
        this.loading.set(false);
      },
    });
  }
}
