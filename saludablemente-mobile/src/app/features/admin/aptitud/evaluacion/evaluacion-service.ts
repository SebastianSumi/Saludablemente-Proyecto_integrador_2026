import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../../core/services/api-service';
import { Page } from '../../../../shared/models/page.model';
import {
  AptitudAgregado,
  Evaluacion,
  EvaluacionFiltros,
  EvaluacionRequest,
  EvaluacionResumen,
  ObservacionesRequest,
} from './evaluacion.model';

@Injectable({ providedIn: 'root' })
export class EvaluacionService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/aptitud';

  // -------------------------------------------------------------------
  // Consultas
  // -------------------------------------------------------------------

  /**
   * Búsqueda paginada con filtros opcionales.
   * Cualquier filtro vacío/null se omite del query string.
   */
  buscar(
    filtros: EvaluacionFiltros,
    page = 0,
    size = 20,
  ): Observable<Page<EvaluacionResumen>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (filtros.idPeriodo != null) {
      params = params.set('idPeriodo', filtros.idPeriodo);
    }
    if (filtros.estado) {
      params = params.set('estado', filtros.estado);
    }
    if (filtros.area) {
      params = params.set('area', filtros.area);
    }
    if (filtros.texto) {
      params = params.set('texto', filtros.texto);
    }

    return this.http.get<Page<EvaluacionResumen>>(
      this.api.buildUrl(this.resource),
      { params },
    );
  }

  /** Evaluación completa: cabecera + detalleFuerza + detalleCardio. */
  obtener(id: number): Observable<Evaluacion> {
    return this.http.get<Evaluacion>(
      this.api.buildUrl(`${this.resource}/${id}`),
    );
  }

  /**
   * Historial de evaluaciones de una persona (orden descendente por fecha).
   * No lanza 404 si la persona no tiene evaluaciones: devuelve array vacío.
   */
  historial(idPersona: number): Observable<EvaluacionResumen[]> {
    return this.http.get<EvaluacionResumen[]>(
      this.api.buildUrl(`${this.resource}/persona/${idPersona}`),
    );
  }

  /** Métricas agregadas de un período (para cards del list y dashboard). */
  obtenerAgregados(idPeriodo: number): Observable<AptitudAgregado> {
    return this.http.get<AptitudAgregado>(
      this.api.buildUrl(`${this.resource}/agregados/${idPeriodo}`),
    );
  }

  // -------------------------------------------------------------------
  // Comandos
  // -------------------------------------------------------------------

  /**
   * Inicia una nueva evaluación de aptitud.
   * El backend valida: período vigente, persona activa, unicidad semestral.
   */
  iniciar(request: EvaluacionRequest): Observable<Evaluacion> {
    return this.http.post<Evaluacion>(
      this.api.buildUrl(this.resource),
      request,
    );
  }

  /** Baja lógica de una evaluación (estado → ANULADA). */
  anular(id: number): Observable<void> {
    return this.http.delete<void>(
      this.api.buildUrl(`${this.resource}/${id}`),
    );
  }

  /** Actualiza las observaciones/restricciones de la cabecera. */
  actualizarObservaciones(
    id: number,
    request: ObservacionesRequest,
  ): Observable<Evaluacion> {
    return this.http.put<Evaluacion>(
      this.api.buildUrl(`${this.resource}/${id}/observaciones`),
      request,
    );
  }
}
