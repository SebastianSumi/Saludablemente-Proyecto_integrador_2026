import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { NotificacionService } from '../notificacion-service';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { Notificacion } from '../models';

@Component({
  selector: 'app-notificacion-detail',
  imports: [DatePipe, RouterLink, TuiButton, TuiIcon],
  templateUrl: './notificacion-detail.html',
  styleUrl: './notificacion-detail.css',
})
export class NotificacionDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(NotificacionService);
  private readonly confirmar = inject(ConfirmService);

  protected readonly item = signal<Notificacion | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isFinite(id)) {
      this.error.set('Identificador de notificación inválido.');
      return;
    }
    this.cargar(id);
  }

  cargar(id: number): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.obtener(id).subscribe({
      next: (data) => {
        this.item.set(data);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 404
            ? 'La notificación no existe.'
            : 'No se pudo cargar la notificación.',
        );
        this.loading.set(false);
      },
    });
  }

  marcarLeida(): void {
    const n = this.item();
    if (!n) return;
    this.service.marcarLeida(n.idNotificacion).subscribe({
      next: () => this.cargar(n.idNotificacion),
      error: () => this.error.set('No se pudo marcar como leída.'),
    });
  }

  archivar(): void {
    const n = this.item();
    if (!n) return;
    this.confirmar
      .confirmar(`¿Archivar la notificación "${n.titulo}"?`, 'Archivar')
      .subscribe((ok) => {
        if (!ok) return;
        this.service.archivar(n.idNotificacion).subscribe({
          next: () => this.router.navigate(['/notificaciones']),
          error: (err: HttpErrorResponse) => {
            this.error.set(err.error?.message ?? 'No se pudo archivar.');
          },
        });
      });
  }

  protected rutaOrigen(): string | null {
    return rutaOrigen(this.item()?.origenModulo);
  }
}

const RUTAS_ORIGEN: Record<string, string> = {
  alertaclinica: '/alertaclinica/alertas',
  alertasclinicas: '/alertaclinica/alertas',
  asistencias: '/asistencias/registro',
  metas: '/metas',
  actividades: '/actividades',
  personal: '/personal/personas',
  noticias: '/noticias',
  nutricional: '/nutricional/evaluaciones',
  aptitud: '/aptitud',
  seguridad: '/seguridad/usuarios',
  catalogos: '/catalogos/areas',
};

export function rutaOrigen(origenModulo: string | undefined): string | null {
  if (!origenModulo) return null;
  const clave = origenModulo.toLowerCase().replace(/[_-]/g, '');
  return RUTAS_ORIGEN[clave] ?? null;
}
