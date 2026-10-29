import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api-service';
import { Participacion, QrActividad } from './models';

@Injectable({ providedIn: 'root' })
export class ReporteAsistenciaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);

  participacion(idActividad: number): Observable<Participacion> {
    return this.http.get<Participacion>(
      this.api.buildUrl(`/api/v1/asistencias/reportes/participacion/${idActividad}`)
    );
  }

  ausentismo(minRacha = 3): Observable<{ minRacha: number; total: number; idsPersona: number[] }> {
    return this.http.get<{ minRacha: number; total: number; idsPersona: number[] }>(
      this.api.buildUrl('/api/v1/asistencias/reportes/ausentismo'),
      { params: { minRacha: String(minRacha) } }
    );
  }

  gamificacion(idActividad: number): Observable<number[]> {
    return this.http.get<number[]>(
      this.api.buildUrl(`/api/v1/asistencias/reportes/gamificacion/${idActividad}`)
    );
  }
}

@Injectable({ providedIn: 'root' })
export class QrActividadService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);

  actual(idActividad: number): Observable<QrActividad> {
    return this.http.get<QrActividad>(
      this.api.buildUrl(`/api/v1/actividades/${idActividad}/qr/actual`)
    );
  }

  rotar(idActividad: number): Observable<QrActividad> {
    return this.http.post<QrActividad>(
      this.api.buildUrl(`/api/v1/actividades/${idActividad}/qr/rotar`),
      null
    );
  }
}

@Injectable({ providedIn: 'root' })
export class ActividadService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);

  listar(): Observable<import('./models').Actividad[]> {
    return this.http.get<import('./models').Actividad[]>(
      this.api.buildUrl('/api/v1/agenda/actividades')
    );
  }

  deHoy(): Observable<import('./models').Actividad[]> {
    return this.http.get<import('./models').Actividad[]>(
      this.api.buildUrl('/api/v1/agenda/hoy')
    );
  }
}
