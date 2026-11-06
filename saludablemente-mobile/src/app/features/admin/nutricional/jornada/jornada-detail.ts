import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { TuiButtonLoading } from '@taiga-ui/kit';
import { AuthService } from '../../../../core/auth/auth-service';
import { JornadaService } from './jornada-service';
import {
  EstadoJornada,
  InscripcionJornadaResponse,
  JornadaEvaluacionResponse,
} from './jornada.model';

@Component({
  selector: 'app-jornada-detail',
  imports: [RouterLink, TuiButton, TuiButtonLoading, TuiIcon],
  templateUrl: './jornada-detail.html',
  styleUrl: './jornada-detail.css',
})
export class JornadaDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly jornadaService = inject(JornadaService);
  private readonly auth = inject(AuthService);

  protected readonly jornada = signal<JornadaEvaluacionResponse | null>(null);
  protected readonly inscripciones = signal<InscripcionJornadaResponse[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly exito = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly inscribiendo = signal(false);

  protected readonly esAdmin = computed(() => this.auth.tieneRol('Administrador'));

  protected readonly miInscripcion = computed<InscripcionJornadaResponse | null>(
    () =>
      this.inscripciones().find(
        (i) =>
          i.idJornada === this.idJornada &&
          (i.estado === 'ACTIVA' || i.estado === 'ASISTIO')
      ) ?? null
  );

  protected readonly puedeInscribirse = computed(() => {
    const j = this.jornada();
    if (!j) return false;
    return (
      j.estado === 'ABIERTA' &&
      j.cuposDisponibles > 0 &&
      this.miInscripcion() === null
    );
  });

  private idJornada = 0;

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      this.error.set('ID de jornada no válido.');
      return;
    }
    this.idJornada = Number(idParam);
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);

    forkJoin({
      jornada: this.jornadaService.obtener(this.idJornada),
      inscripciones: this.jornadaService
        .misInscripciones()
        .pipe(catchError(() => of([] as InscripcionJornadaResponse[]))),
    }).subscribe({
      next: ({ jornada, inscripciones }) => {
        this.jornada.set(jornada);
        this.inscripciones.set(inscripciones);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 404
            ? 'La jornada solicitada no existe.'
            : 'No se pudo cargar la jornada.'
        );
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  inscribirme(): void {
    if (!this.puedeInscribirse() || this.inscribiendo()) return;

    this.inscribiendo.set(true);
    this.error.set(null);
    this.exito.set(null);

    this.jornadaService.inscribirme(this.idJornada).subscribe({
      next: (inscripcion) => {
        this.inscripciones.update((actuales) => [
          inscripcion,
          ...actuales.filter((i) => i.idInscripcion !== inscripcion.idInscripcion),
        ]);
        this.exito.set(
          `Inscripción confirmada. Tu número de turno es ${inscripcion.numeroTurno}.`
        );
        this.inscribiendo.set(false);
        this.cargar();
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 409
            ? (err.error?.message ?? 'No se pudo completar la inscripción.')
            : 'No se pudo completar la inscripción.'
        );
        this.inscribiendo.set(false);
      },
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

  protected claseEstado(estado: EstadoJornada): string {
    switch (estado) {
      case 'ABIERTA':
        return 'pill pill-success';
      case 'CERRADA':
        return 'pill pill-neutral';
      case 'CANCELADA':
        return 'pill pill-error';
      default:
        return 'pill pill-warning';
    }
  }

  protected etiquetaEstado(estado: EstadoJornada): string {
    switch (estado) {
      case 'ABIERTA':
        return 'Abierta';
      case 'CERRADA':
        return 'Cerrada';
      case 'CANCELADA':
        return 'Cancelada';
      default:
        return 'Programada';
    }
  }

  protected claseInscripcion(estado: string): string {
    switch (estado) {
      case 'ACTIVA':
        return 'pill pill-success';
      case 'ASISTIO':
        return 'pill pill-soft-primary';
      default:
        return 'pill pill-neutral';
    }
  }
}
