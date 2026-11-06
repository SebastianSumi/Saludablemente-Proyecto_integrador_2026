import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { TuiTable } from '@taiga-ui/addon-table';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { NotificacionService } from '../notificacion-service';
import { PerfilService } from '../../perfil/perfil-service';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { BandejaNotificacion } from '../models';

@Component({
  selector: 'app-notificacion-bandeja',
  imports: [TuiButton, TuiIcon, TuiTable],
  templateUrl: './bandeja.html',
  styleUrl: './notificacion-list.css',
})
export class NotificacionBandeja implements OnInit {
  private readonly service = inject(NotificacionService);
  private readonly perfilSrv = inject(PerfilService);
  private readonly confirmar = inject(ConfirmService);

  protected readonly items = signal<BandejaNotificacion[]>([]);
  protected readonly noLeidas = signal(0);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  private idPersona = 0;

  ngOnInit(): void {
    this.perfilSrv
      .obtener()
      .pipe(catchError(() => of(null)))
      .subscribe((p) => {
        if (!p) {
          this.error.set('No se pudo identificar al colaborador. Recarga la página.');
          return;
        }
        this.idPersona = p.idPersona;
        this.cargar();
      });
  }

  cargar(): void {
    if (!this.idPersona) return;
    this.loading.set(true);
    this.error.set(null);
    this.service.misAlertas(this.idPersona).subscribe({
      next: (data) => {
        this.items.set(data ?? []);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 0 ? 'No se pudo conectar con el servidor.' : 'No se pudo cargar tu bandeja.',
        );
        this.loading.set(false);
      },
    });
    this.service.contadorNoLeidas(this.idPersona).subscribe({
      next: (n) => this.noLeidas.set(n ?? 0),
      error: () => this.noLeidas.set(0),
    });
  }

  marcarLeida(item: BandejaNotificacion): void {
    this.service.marcarLeida(item.idNotificacion).subscribe({
      next: () => this.cargar(),
      error: () => this.error.set('No se pudo marcar como leída.'),
    });
  }

  archivar(item: BandejaNotificacion): void {
    this.confirmar
      .confirmar(`¿Archivar "${item.titulo}"?`, 'Archivar')
      .subscribe((ok) => {
        if (!ok) return;
        this.service.archivar(item.idNotificacion).subscribe({
          next: () => this.cargar(),
          error: () => this.error.set('No se pudo archivar.'),
        });
      });
  }
}
