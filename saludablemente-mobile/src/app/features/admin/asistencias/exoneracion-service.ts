import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api-service';
import {
  Ausentismo,
  DictamenRequest,
  EstadoJustificacion,
  Justificacion,
  JustificacionRequest,
  Pagina,
} from './models';

@Injectable({ providedIn: 'root' })
export class ExoneracionService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/exoneraciones';

  solicitar(request: JustificacionRequest): Observable<Justificacion> {
    return this.http.post<Justificacion>(
      this.api.buildUrl(`${this.resource}/solicitar`),
      request
    );
  }

  dictaminar(id: number, request: DictamenRequest): Observable<Justificacion> {
    return this.http.put<Justificacion>(
      this.api.buildUrl(`${this.resource}/${id}/dictaminar`),
      request
    );
  }

  listar(filtros: {
    estado?: EstadoJustificacion;
    idPersona?: number;
    desde?: string;
    hasta?: string;
    page?: number;
    size?: number;
  } = {}): Observable<Pagina<Justificacion>> {
    let params = new HttpParams();
    if (filtros.estado) params = params.set('estado', filtros.estado);
    if (filtros.idPersona) params = params.set('idPersona', String(filtros.idPersona));
    if (filtros.desde) params = params.set('desde', filtros.desde);
    if (filtros.hasta) params = params.set('hasta', filtros.hasta);
    params = params.set('page', String(filtros.page ?? 0));
    params = params.set('size', String(filtros.size ?? 20));
    return this.http.get<Pagina<Justificacion>>(this.api.buildUrl(this.resource), {
      params,
    });
  }

  detectarAusentismo(minRacha = 3): Observable<Ausentismo> {
    return this.http.get<Ausentismo>(
      this.api.buildUrl(`${this.resource}/ausentismo`),
      { params: new HttpParams().set('minRacha', String(minRacha)) }
    );
  }
}
