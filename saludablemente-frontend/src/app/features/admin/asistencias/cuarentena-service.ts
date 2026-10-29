import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api-service';
import { Cuarentena, CuarentenaFiltros, Pagina, ResolverCuarentenaRequest } from './models';

@Injectable({ providedIn: 'root' })
export class CuarentenaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/cuarentena';

  listar(filtros: CuarentenaFiltros = {}): Observable<Pagina<Cuarentena>> {
    let params = new HttpParams();
    if (filtros.estado) params = params.set('estado', filtros.estado);
    if (filtros.motivo) params = params.set('motivo', filtros.motivo);
    if (filtros.idActividad) params = params.set('idActividad', String(filtros.idActividad));
    if (filtros.idPersona) params = params.set('idPersona', String(filtros.idPersona));
    if (filtros.desde) params = params.set('desde', filtros.desde);
    if (filtros.hasta) params = params.set('hasta', filtros.hasta);
    params = params.set('page', String(filtros.page ?? 0));
    params = params.set('size', String(filtros.size ?? 20));
    return this.http.get<Pagina<Cuarentena>>(this.api.buildUrl(this.resource), { params });
  }

  obtener(id: number): Observable<Cuarentena> {
    return this.http.get<Cuarentena>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  resolver(id: number, request: ResolverCuarentenaRequest): Observable<Cuarentena> {
    return this.http.put<Cuarentena>(
      this.api.buildUrl(`${this.resource}/${id}/resolver`),
      request
    );
  }

  contarPendientes(): Observable<{ total: number }> {
    return this.http.get<{ total: number }>(
      this.api.buildUrl(`${this.resource}/pendientes/count`)
    );
  }
}
