import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { AgendaService } from '../../admin/actividades/agenda-service';
import { Actividad, ActividadImagen, urlImagenActividad } from '../../admin/actividades/models';

@Component({
  selector: 'app-col-mis-actividades-detalle',
  standalone: true,
  imports: [RouterLink, DatePipe, TuiButton, TuiIcon],
  templateUrl: './col-mis-actividades-detalle.html',
  styleUrl: './col-mis-actividades-detalle.css',
})
export class ColMisActividadesDetalle implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly agendaSrv = inject(AgendaService);

  protected readonly idActividad = signal<number | null>(null);
  protected readonly actividad = signal<Actividad | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly fotoSeleccionada = signal<ActividadImagen | null>(null);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      const numId = Number(id);
      this.idActividad.set(numId);
      this.cargar(numId);
    }
  }

  cargar(id: number): void {
    this.loading.set(true);
    this.error.set(null);
    this.agendaSrv.obtener(id).subscribe({
      next: (act) => {
        this.actividad.set(act);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 404
            ? 'Actividad no encontrada.'
            : 'No se pudo cargar el detalle de la actividad.'
        );
        this.loading.set(false);
      },
    });
  }

  protected resolverUrl(url?: string | null): string | undefined {
    return urlImagenActividad(url);
  }

  protected abrirFoto(img: ActividadImagen): void {
    this.fotoSeleccionada.set(img);
  }

  protected cerrarFoto(): void {
    this.fotoSeleccionada.set(null);
  }
}
