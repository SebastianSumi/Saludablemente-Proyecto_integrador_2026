import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { TuiTable } from '@taiga-ui/addon-table';
import { JornadaService } from './jornada-service';
import { InscripcionJornadaResponse } from './jornada.model';

@Component({
  selector: 'app-mis-inscripciones',
  imports: [RouterLink, TuiButton, TuiIcon, TuiTable],
  templateUrl: './mis-inscripciones.html',
  styleUrl: './mis-inscripciones.css',
})
export class MisInscripciones implements OnInit {
  private readonly jornadaService = inject(JornadaService);

  protected readonly inscripciones = signal<InscripcionJornadaResponse[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);

    this.jornadaService.misInscripciones().subscribe({
      next: (data) => this.inscripciones.set(data),
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 0
            ? 'No se pudo conectar con el servidor.'
            : 'No se pudieron cargar tus inscripciones.'
        );
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  protected formatearFecha(fecha: string | null): string {
    if (!fecha) return '—';
    const [anio, mes, dia] = fecha.split('-');
    return `${dia}/${mes}/${anio}`;
  }

  protected formatearHora(hora: string | null): string {
    if (!hora) return '—';
    return hora.slice(0, 5);
  }

  protected claseEstado(estado: string): string {
    switch (estado) {
      case 'ACTIVA':
        return 'pill pill-success';
      case 'ASISTIO':
        return 'pill pill-soft-primary';
      default:
        return 'pill pill-neutral';
    }
  }

  protected etiquetaEstado(estado: string): string {
    switch (estado) {
      case 'ACTIVA':
        return 'Activa';
      case 'ASISTIO':
        return 'Asistió';
      default:
        return 'Cancelada';
    }
  }
}
