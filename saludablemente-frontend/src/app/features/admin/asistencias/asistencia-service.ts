import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { ApiService } from '../../../core/services/api-service';
import {
  Asistencia,
  AsistenciaActividad,
  AsistenciaManualDetalleRequest,
  AsistenciaHistorial,
  AsistenciaManualRequest,
  AsistenciaQrRequest,
  CierreAsistencias,
  MarcacionLoteRequest,
  MarcacionLoteResponse,
  MarcacionRequest,
} from './models';

@Injectable({ providedIn: 'root' })
export class AsistenciaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/asistencias';

  registrarManual(request: AsistenciaManualRequest): Observable<Asistencia> {
    return this.http.post<Asistencia>(
      this.api.buildUrl(`${this.resource}/manual`),
      request
    );
  }

  registrarPorQr(request: AsistenciaQrRequest): Observable<Asistencia> {
    return this.http.post<Asistencia>(
      this.api.buildUrl(`${this.resource}/qr/coordinador`),
      request
    );
  }

  listarPorActividad(idActividad: number): Observable<Asistencia[]> {
    return this.consultarDetalle(idActividad).pipe(map((r) => r.detalle));
  }

  consultarDetalle(idActividad: number): Observable<AsistenciaActividad> {
    return this.http.get<AsistenciaActividad>(
      this.api.buildUrl(`${this.resource}/manual/actividad/${idActividad}`)
    );
  }

  guardarManualDetalle(request: AsistenciaManualDetalleRequest): Observable<AsistenciaActividad> {
    return this.http.post<AsistenciaActividad>(
      this.api.buildUrl(`${this.resource}/manual/detalle`), request
    );
  }

  historialPorPersona(idPersona: number): Observable<AsistenciaHistorial[]> {
    return this.http.get<AsistenciaHistorial[]>(
      this.api.buildUrl(`${this.resource}/historial/${idPersona}`)
    );
  }

  forzarCierre(idActividad: number): Observable<CierreAsistencias> {
    return this.http.post<CierreAsistencias>(
      this.api.buildUrl(`${this.resource}/cierre/${idActividad}`),
      null
    );
  }

  marcar(request: MarcacionRequest): Observable<Asistencia> {
    return this.http.post<Asistencia>(
      this.api.buildUrl(`${this.resource}/marcacion`),
      request
    );
  }

  marcarLote(request: MarcacionLoteRequest): Observable<MarcacionLoteResponse> {
    return this.http.post<MarcacionLoteResponse>(
      this.api.buildUrl(`${this.resource}/marcacion/lote`),
      request
    );
  }
}
