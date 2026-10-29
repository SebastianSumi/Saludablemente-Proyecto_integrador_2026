import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { TuiBadge } from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { NotificacionService } from '../notificacion-service';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { Notificacion } from '../models';

@Component({
  selector: 'app-notificacion-list',
  imports: [RouterLink, TuiBadge, TuiButton, TuiIcon, TuiTable],
  templateUrl: './notificacion-list.html',
  styleUrl: './notificacion-list.css',
})
export class NotificacionList implements OnInit {
  private readonly service = inject(NotificacionService);
  private readonly confirmar = inject(ConfirmService);

  protected readonly items = signal<Notificacion[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.listar().subscribe({
      next: (data) => {
        this.items.set(data ?? []);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 0
            ? 'No se pudo conectar con el servidor.'
            : 'No se pudo cargar las notificaciones.',
        );
        this.loading.set(false);
      },
    });
  }

  marcarLeida(item: Notificacion): void {
    this.service.marcarLeida(item.idNotificacion).subscribe({
      next: () => this.cargar(),
      error: () => this.error.set('No se pudo marcar como leída.'),
    });
  }

  archivar(item: Notificacion): void {
    this.confirmar
      .confirmar(`¿Archivar la notificación "${item.titulo}"?`, 'Archivar')
      .subscribe((ok) => {
        if (!ok) return;
        this.service.archivar(item.idNotificacion).subscribe({
          next: () => this.cargar(),
          error: (err: HttpErrorResponse) => {
            this.error.set(err.error?.message ?? 'No se pudo archivar.');
          },
        });
      });
  }
}
