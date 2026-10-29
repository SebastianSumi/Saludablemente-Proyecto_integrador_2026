import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../../core/services/api-service';
import {
  AlertaClinica,
  AlertaReporte,
  AtenderAlertaRequest,
  AlertaFiltros,
} from './alerta-clinica.model';

@Injectable({ providedIn: 'root' })
export class AlertaClinicaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/alertas';

  listar(filtros: AlertaFiltros = {}): Observable<AlertaClinica[]> {
    let params = new HttpParams();
    if (filtros.etiqueta)   params = params.set('etiqueta', filtros.etiqueta);
    if (filtros.estado)     params = params.set('estado', filtros.estado);
    if (filtros.severidad)  params = params.set('severidad', filtros.severidad);
    if (filtros.personaId)  params = params.set('personaId', String(filtros.personaId));
    if (filtros.tipoOrigen) params = params.set('tipoOrigen', filtros.tipoOrigen);
    if (filtros.desde)      params = params.set('desde', filtros.desde);
    if (filtros.hasta)      params = params.set('hasta', filtros.hasta);
    if (filtros.ordenarPor) params = params.set('ordenarPor', filtros.ordenarPor);
    if (filtros.direccion)  params = params.set('direccion', filtros.direccion);

    return this.http.get<AlertaClinica[]>(this.api.buildUrl(this.resource), { params });
  }

  obtener(id: number): Observable<AlertaClinica> {
    return this.http.get<AlertaClinica>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  reporte(desde?: string, hasta?: string): Observable<AlertaReporte> {
    let params = new HttpParams();
    if (desde) params = params.set('desde', desde);
    if (hasta) params = params.set('hasta', hasta);
    return this.http.get<AlertaReporte>(this.api.buildUrl(`${this.resource}/resumen`), { params });
  }

  atender(id: number, request: AtenderAlertaRequest): Observable<AlertaClinica> {
    return this.http.patch<AlertaClinica>(
      this.api.buildUrl(`${this.resource}/${id}/atender`),
      request,
    );
  }

  anular(id: number): Observable<AlertaClinica> {
    return this.http.patch<AlertaClinica>(
      this.api.buildUrl(`${this.resource}/${id}/anular`),
      {},
    );
  }
}
