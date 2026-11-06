import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { ApiService } from '../../../core/services/api-service';
import type { AlertaClinica, EstadoAlerta, SeveridadAlerta, TipoEvaluacionOrigen } from './alerta.model';

export interface ColAlertaFiltros {
  estado?: EstadoAlerta;
  severidad?: SeveridadAlerta;
  tipoOrigen?: TipoEvaluacionOrigen;
}

/** La identidad y pertenencia se resuelven en el servidor, nunca con un personaId del cliente. */
@Injectable({ providedIn: 'root' })
export class ColAlertaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly recurso = '/api/v1/mis-alertas';
  listarMias(filtros: ColAlertaFiltros = {}): Observable<AlertaClinica[]> {
    let params = new HttpParams();
    if (filtros.estado) params = params.set('estado', filtros.estado);
    if (filtros.severidad) params = params.set('severidad', filtros.severidad);
    if (filtros.tipoOrigen) params = params.set('tipoOrigen', filtros.tipoOrigen);
    return this.http.get<AlertaClinica[]>(this.api.buildUrl(this.recurso), { params });
  }
  obtenerMio(id: number): Observable<AlertaClinica> {
    if (!Number.isSafeInteger(id) || id <= 0) return throwError(() => new Error('Identificador inválido.'));
    return this.http.get<AlertaClinica>(this.api.buildUrl(`${this.recurso}/${id}`));
  }
}
