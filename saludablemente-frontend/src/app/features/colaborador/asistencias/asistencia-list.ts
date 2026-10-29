import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { TuiButton, TuiTitle } from '@taiga-ui/core';
import { TuiAvatar, TuiBadge } from '@taiga-ui/kit';
import { TuiCardLarge, TuiHeader } from '@taiga-ui/layout';
import { TuiTable } from '@taiga-ui/addon-table';
import { forkJoin, of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { PerfilService } from '../../admin/perfil/perfil-service';
import { AsistenciaService } from '../../admin/asistencias/asistencia-service';
import type { AsistenciaHistorial } from '../../admin/asistencias/models';
import { AgendaService } from '../../admin/actividades/agenda-service';
import type { Actividad } from '../../admin/actividades/models';

@Component({
  selector: 'app-col-asistencia-list',
  standalone: true,
  imports: [RouterLink, TuiButton, TuiBadge, TuiAvatar, TuiCardLarge, TuiHeader, TuiTitle, TuiTable, DatePipe],
  templateUrl: './asistencia-list.html',
  styleUrls: ['./asistencia-list.css'],
})
export class ColAsistenciaList implements OnInit {
  private readonly perfilSrv = inject(PerfilService);
  private readonly asisSrv = inject(AsistenciaService);
  private readonly agendaSrv = inject(AgendaService);

  protected readonly historial = signal<AsistenciaHistorial[]>([]);
  protected readonly hoy = signal<Actividad[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.loading.set(true);
    this.perfilSrv
      .obtener()
      .pipe(
        switchMap((p) =>
          forkJoin({
            hist: this.asisSrv.historialPorPersona(p.idPersona).pipe(catchError(() => of([] as AsistenciaHistorial[]))),
            hoy: this.agendaSrv.deHoy().pipe(catchError(() => of([] as Actividad[]))),
          }),
        ),
      )
      .subscribe({
        next: ({ hist, hoy }) => {
          this.historial.set(hist);
          this.hoy.set(hoy);
          this.loading.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.error.set(err.status === 0 ? 'No se pudo conectar con el servidor.' : 'No se pudo cargar tus asistencias.');
          this.loading.set(false);
        },
      });
  }

  protected estadoBadge(e: string): string {
    return e === 'ASISTIO'
      ? 'positive'
      : e === 'TARDE'
        ? 'warning'
        : e === 'AUSENTE'
          ? 'negative'
          : e === 'EXONERADO'
            ? 'accent'
            : 'neutral';
  }
}
