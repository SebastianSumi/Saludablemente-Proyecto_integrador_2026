import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../../../core/services/api-service';
import {
  ReglaPorcentajeGrasaResponse,
  ReglaPorcentajeGrasaRequest,
  ReglaPorcentajeGrasaFiltros,
} from './regla-porcentaje-grasa.model';

@Injectable({ providedIn: 'root' })
export class ReglaPorcentajeGrasaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/reglas/porcentaje-grasa';

  listar(filtros: ReglaPorcentajeGrasaFiltros = {}): Observable<ReglaPorcentajeGrasaResponse[]> {
    let params = new HttpParams();
    if (filtros.estado) params = params.set('estado', filtros.estado);
    if (filtros.sexo) params = params.set('sexo', filtros.sexo);

    return this.http.get<ReglaPorcentajeGrasaResponse[]>(
      this.api.buildUrl(this.resource),
      { params },
    );
  }

  obtener(id: number): Observable<ReglaPorcentajeGrasaResponse> {
    return this.http.get<ReglaPorcentajeGrasaResponse>(
      this.api.buildUrl(`${this.resource}/${id}`),
    );
  }

  crear(regla: ReglaPorcentajeGrasaRequest): Observable<ReglaPorcentajeGrasaResponse> {
    return this.http.post<ReglaPorcentajeGrasaResponse>(
      this.api.buildUrl(this.resource),
      regla,
    );
  }

  actualizar(id: number, regla: ReglaPorcentajeGrasaRequest): Observable<ReglaPorcentajeGrasaResponse> {
    return this.http.put<ReglaPorcentajeGrasaResponse>(
      this.api.buildUrl(`${this.resource}/${id}`),
      regla,
    );
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  reactivar(id: number): Observable<ReglaPorcentajeGrasaResponse> {
    return this.http.patch<ReglaPorcentajeGrasaResponse>(
      this.api.buildUrl(`${this.resource}/${id}/reactivar`),
      {},
    );
  }
}
