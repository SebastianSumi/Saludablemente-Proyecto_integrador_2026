import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../../core/services/api-service';
import { Periodo } from '../../catalogo/periodo/periodo.model';
import {
  EstadoEvaluacion,
  EvaluacionAgregado,
  EvaluacionInitRequest,
  EvaluacionResponse,
  EvaluacionResumen,
  Page,
} from './evaluacion.model';

@Injectable({ providedIn: 'root' })
export class EvaluacionService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/evaluaciones-nutricionales';

  // ---------------- Consultas ----------------

  buscar(
    estado?: EstadoEvaluacion,
    periodo?: string,
    idPersona?: number,
    page = 0,
    size = 20
  ): Observable<Page<EvaluacionResumen>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);
    if (estado) params = params.set('estado', estado);
    if (periodo) params = params.set('periodo', periodo);
    if (idPersona) params = params.set('idPersona', idPersona);

    return this.http.get<Page<EvaluacionResumen>>(
      this.api.buildUrl(this.resource),
      { params }
    );
  }

  obtener(id: number): Observable<EvaluacionResponse> {
    return this.http.get<EvaluacionResponse>(
      this.api.buildUrl(`${this.resource}/${id}`)
    );
  }

  historialPorPersona(idPersona: number): Observable<EvaluacionResumen[]> {
    return this.http.get<EvaluacionResumen[]>(
      this.api.buildUrl(`${this.resource}/persona/${idPersona}`)
    );
  }

  incompletas(periodo: string): Observable<EvaluacionResumen[]> {
    const params = new HttpParams().set('periodo', periodo);
    return this.http.get<EvaluacionResumen[]>(
      this.api.buildUrl(`${this.resource}/incompletas`),
      { params }
    );
  }

  agregados(periodo?: string): Observable<EvaluacionAgregado> {
    let params = new HttpParams();
    if (periodo) params = params.set('periodo', periodo);
    return this.http.get<EvaluacionAgregado>(
      this.api.buildUrl(`${this.resource}/agregados`),
      { params }
    );
  }

  periodosDisponibles(): Observable<Periodo[]> {
    return this.http.get<Periodo[]>(
      this.api.buildUrl(`${this.resource}/periodos-disponibles`)
    );
  }

  // ---------------- Comandos ----------------

  iniciar(request: EvaluacionInitRequest): Observable<EvaluacionResponse> {
    return this.http.post<EvaluacionResponse>(
      this.api.buildUrl(this.resource),
      request
    );
  }

  actualizarObservaciones(id: number, observaciones: string): Observable<EvaluacionResponse> {
    return this.http.patch<EvaluacionResponse>(
      this.api.buildUrl(`${this.resource}/${id}/observaciones`),
      { observaciones }
    );
  }

  anular(id: number): Observable<void> {
    return this.http.delete<void>(
      this.api.buildUrl(`${this.resource}/${id}`)
    );
  }

  // ---------------- Reportes ----------------

  exportarConsolidado(periodo: string): Observable<Blob> {
    return this.http.get(
      this.api.buildUrl(`${this.resource}/exportar/${periodo}`),
      { responseType: 'blob' }
    );
  }

  descargarPdf(id: number): Observable<Blob> {
    return this.http.get(
      this.api.buildUrl(`${this.resource}/${id}/pdf`),
      { responseType: 'blob' }
    );
  }
}
