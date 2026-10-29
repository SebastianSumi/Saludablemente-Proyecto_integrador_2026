import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { TuiBadge } from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { AgendaService } from '../agenda-service';
import { Actividad, urlImagenActividad } from '../models';
import { ActividadCalendario } from './actividad-calendario';

export type VistaAgenda = 'calendario' | 'agenda';

@Component({
  selector: 'app-actividad-list',
  imports: [RouterLink, TuiBadge, TuiButton, TuiIcon, TuiTable, ActividadCalendario],
  templateUrl: './actividad-list.html',
  styleUrl: './actividad-list.css',
})
export class ActividadList implements OnInit {
  private readonly service = inject(AgendaService);

  protected readonly items = signal<Actividad[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly vista = signal<VistaAgenda>('calendario');

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.listar().subscribe({
      next: (data) => {
        this.items.set(data);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 0 ? 'No se pudo conectar con el servidor.' : 'No se pudo cargar las actividades.'
        );
        this.loading.set(false);
      },
    });
  }

  protected resolverUrl(url?: string | null): string | undefined {
    return urlImagenActividad(url);
  }

  protected aparienciaEstado(e?: string): string {
    switch (e) {
      case 'EN_CURSO':
        return 'primary';
      case 'FINALIZADA':
        return 'positive';
      case 'CANCELADA':
        return 'negative';
      default:
        return 'warning';
    }
  }
}
