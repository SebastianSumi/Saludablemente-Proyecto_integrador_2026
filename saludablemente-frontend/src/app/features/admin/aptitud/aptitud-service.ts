import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api-service';
import {
  AptitudAgregado,
  CardioDetalle,
  CardioRequest,
  EvaluacionAptitud,
  EvaluacionFiltros,
  EvaluacionInitRequest,
  EvaluacionResumen,
  FuerzaDetalle,
  FuerzaRequest,
  Pagina,
} from './models';

@Injectable({ providedIn: 'root' })
export class AptitudService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/aptitud';

  iniciar(request: EvaluacionInitRequest): Observable<EvaluacionAptitud> {
    return this.http.post<EvaluacionAptitud>(
      this.api.buildUrl(this.resource),
      request
    );
  }

  buscar(filtros: EvaluacionFiltros = {}): Observable<Pagina<EvaluacionResumen>> {
    let params = new HttpParams();
    if (filtros.idPeriodo) params = params.set('idPeriodo', String(filtros.idPeriodo));
    if (filtros.estado) params = params.set('estado', filtros.estado);
    if (filtros.area) params = params.set('area', filtros.area);
    if (filtros.texto) params = params.set('texto', filtros.texto);
    params = params.set('page', String(filtros.page ?? 0));
    params = params.set('size', String(filtros.size ?? 20));
    return this.http.get<Pagina<EvaluacionResumen>>(this.api.buildUrl(this.resource), {
      params,
    });
  }

  obtener(id: number): Observable<EvaluacionAptitud> {
    return this.http.get<EvaluacionAptitud>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  historial(idPersona: number): Observable<EvaluacionResumen[]> {
    return this.http.get<EvaluacionResumen[]>(
      this.api.buildUrl(`${this.resource}/persona/${idPersona}`)
    );
  }

  anular(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  actualizarObservaciones(id: number, observaciones: string): Observable<EvaluacionAptitud> {
    return this.http.put<EvaluacionAptitud>(
      this.api.buildUrl(`${this.resource}/${id}/observaciones`),
      { observaciones }
    );
  }

  agregados(idPeriodo: number): Observable<AptitudAgregado> {
    return this.http.get<AptitudAgregado>(
      this.api.buildUrl(`${this.resource}/agregados/${idPeriodo}`)
    );
  }

  registrarFuerza(idEvaluacion: number, request: FuerzaRequest): Observable<FuerzaDetalle> {
    return this.http.post<FuerzaDetalle>(
      this.api.buildUrl(`${this.resource}/${idEvaluacion}/fuerza`),
      request
    );
  }

  actualizarFuerza(idEvaluacion: number, request: FuerzaRequest): Observable<FuerzaDetalle> {
    return this.http.put<FuerzaDetalle>(
      this.api.buildUrl(`${this.resource}/${idEvaluacion}/fuerza`),
      request
    );
  }

  consultarFuerza(idEvaluacion: number): Observable<FuerzaDetalle> {
    return this.http.get<FuerzaDetalle>(
      this.api.buildUrl(`${this.resource}/${idEvaluacion}/fuerza`)
    );
  }

  registrarCardio(idEvaluacion: number, request: CardioRequest): Observable<CardioDetalle> {
    return this.http.post<CardioDetalle>(
      this.api.buildUrl(`${this.resource}/${idEvaluacion}/cardio`),
      request
    );
  }

  actualizarCardio(idEvaluacion: number, request: CardioRequest): Observable<CardioDetalle> {
    return this.http.put<CardioDetalle>(
      this.api.buildUrl(`${this.resource}/${idEvaluacion}/cardio`),
      request
    );
  }

  consultarCardio(idEvaluacion: number): Observable<CardioDetalle> {
    return this.http.get<CardioDetalle>(
      this.api.buildUrl(`${this.resource}/${idEvaluacion}/cardio`)
    );
  }
}
