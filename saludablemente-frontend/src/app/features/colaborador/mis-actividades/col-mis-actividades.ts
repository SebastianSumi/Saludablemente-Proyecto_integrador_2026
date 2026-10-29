import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TuiButton, TuiIcon, TuiTextfield } from '@taiga-ui/core';
import { TuiCalendarRange, TuiInputDateRange } from '@taiga-ui/kit';
import { TuiDayRange } from '@taiga-ui/cdk/date-time';
import { TuiTable } from '@taiga-ui/addon-table';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PerfilService } from '../../admin/perfil/perfil-service';
import { AgendaService } from '../../admin/actividades/agenda-service';
import { Actividad, urlImagenActividad } from '../../admin/actividades/models';
import { fijarHoyEn, limpiarFechaEn } from '../../../core/forms/fecha-transformers';

@Component({
  selector: 'app-col-mis-actividades',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    DatePipe,
    TuiButton,
    TuiIcon,
    TuiTextfield,
    TuiCalendarRange,
    TuiInputDateRange,
    TuiTable,
  ],
  templateUrl: './col-mis-actividades.html',
  styleUrl: './col-mis-actividades.css',
})
export class ColMisActividades implements OnInit {
  private readonly perfilSrv = inject(PerfilService);
  private readonly agendaSrv = inject(AgendaService);

  protected readonly actividades = signal<Actividad[]>([]);
  protected readonly filtradas = signal<Actividad[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  readonly rangoCtrl = new FormControl<TuiDayRange | null>(null);

  ngOnInit(): void {
    this.cargar();

    this.rangoCtrl.valueChanges.subscribe((rango) => {
      this.filtrarPorRango(rango);
    });
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);

    this.perfilSrv.obtener().subscribe({
      next: (perfil) => {
        const idPersona = perfil.idPersona;
        this.agendaSrv.listar().subscribe({
          next: (actividades) => {
            const propias = actividades.filter(
              (a) => a.idUsuarioCoordinador === idPersona
            );
            this.actividades.set(propias);
            this.filtradas.set(propias);
            this.loading.set(false);
          },
          error: (err: HttpErrorResponse) => {
            this.error.set(
              err.status === 0
                ? 'No se pudo conectar con el servidor.'
                : 'No se pudo cargar las actividades.'
            );
            this.loading.set(false);
          },
        });
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 0
            ? 'No se pudo conectar con el servidor.'
            : 'No se pudo cargar tu perfil.'
        );
        this.loading.set(false);
      },
    });
  }

  filtrarPorRango(rango: TuiDayRange | null): void {
    if (!rango) {
      this.filtradas.set(this.actividades());
      return;
    }

    const desde = rango.from?.toLocalNativeDate();
    const hasta = rango.to?.toLocalNativeDate();
    if (!desde || !hasta) {
      this.filtradas.set(this.actividades());
      return;
    }

    this.filtradas.set(
      this.actividades().filter((a) => {
        const f = new Date(a.fechaActividad);
        f.setHours(0, 0, 0, 0);
        return f >= desde && f <= hasta;
      })
    );
  }

  protected resolverUrl(url?: string | null): string | undefined {
    return urlImagenActividad(url);
  }

  protected fijarHoy(): void {
    fijarHoyEn(this.rangoCtrl);
    this.filtrarPorRango(this.rangoCtrl.value);
  }

  protected limpiarCampo(): void {
    limpiarFechaEn(this.rangoCtrl);
    this.filtradas.set(this.actividades());
  }
}
