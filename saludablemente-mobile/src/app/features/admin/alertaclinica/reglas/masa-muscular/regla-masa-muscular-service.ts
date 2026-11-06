import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../../../core/services/api-service';
import {
  ReglaMasaMuscularResponse,
  ReglaMasaMuscularRequest,
  ReglaMasaMuscularFiltros,
} from './regla-masa-muscular.model';

@Injectable({ providedIn: 'root' })
export class ReglaMasaMuscularService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/reglas/masa-muscular';

  listar(filtros: ReglaMasaMuscularFiltros = {}): Observable<ReglaMasaMuscularResponse[]> {
    let params = new HttpParams();
    if (filtros.estado) params = params.set('estado', filtros.estado);
    if (filtros.sexo) params = params.set('sexo', filtros.sexo);

    return this.http.get<ReglaMasaMuscularResponse[]>(
      this.api.buildUrl(this.resource),
      { params },
    );
  }

  obtener(id: number): Observable<ReglaMasaMuscularResponse> {
    return this.http.get<ReglaMasaMuscularResponse>(
      this.api.buildUrl(`${this.resource}/${id}`),
    );
  }

  crear(regla: ReglaMasaMuscularRequest): Observable<ReglaMasaMuscularResponse> {
    return this.http.post<ReglaMasaMuscularResponse>(
      this.api.buildUrl(this.resource),
      regla,
    );
  }

  actualizar(id: number, regla: ReglaMasaMuscularRequest): Observable<ReglaMasaMuscularResponse> {
    return this.http.put<ReglaMasaMuscularResponse>(
      this.api.buildUrl(`${this.resource}/${id}`),
      regla,
    );
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  reactivar(id: number): Observable<ReglaMasaMuscularResponse> {
    return this.http.patch<ReglaMasaMuscularResponse>(
      this.api.buildUrl(`${this.resource}/${id}/reactivar`),
      {},
    );
  }
}
