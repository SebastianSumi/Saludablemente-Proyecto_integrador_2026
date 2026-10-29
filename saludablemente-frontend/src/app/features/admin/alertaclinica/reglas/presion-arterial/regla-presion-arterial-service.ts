import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../../../core/services/api-service';
import {
  ReglaPresionArterialResponse,
  ReglaPresionArterialRequest,
  ReglaPresionArterialFiltros,
} from './regla-presion-arterial.model';

@Injectable({ providedIn: 'root' })
export class ReglaPresionArterialService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/reglas/presion-arterial';

  listar(filtros: ReglaPresionArterialFiltros = {}): Observable<ReglaPresionArterialResponse[]> {
    let params = new HttpParams();
    if (filtros.estado) params = params.set('estado', filtros.estado);
    if (filtros.sexo) params = params.set('sexo', filtros.sexo);

    return this.http.get<ReglaPresionArterialResponse[]>(
      this.api.buildUrl(this.resource),
      { params },
    );
  }

  obtener(id: number): Observable<ReglaPresionArterialResponse> {
    return this.http.get<ReglaPresionArterialResponse>(
      this.api.buildUrl(`${this.resource}/${id}`),
    );
  }

  crear(regla: ReglaPresionArterialRequest): Observable<ReglaPresionArterialResponse> {
    return this.http.post<ReglaPresionArterialResponse>(
      this.api.buildUrl(this.resource),
      regla,
    );
  }

  actualizar(id: number, regla: ReglaPresionArterialRequest): Observable<ReglaPresionArterialResponse> {
    return this.http.put<ReglaPresionArterialResponse>(
      this.api.buildUrl(`${this.resource}/${id}`),
      regla,
    );
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  reactivar(id: number): Observable<ReglaPresionArterialResponse> {
    return this.http.patch<ReglaPresionArterialResponse>(
      this.api.buildUrl(`${this.resource}/${id}/reactivar`),
      {},
    );
  }
}
