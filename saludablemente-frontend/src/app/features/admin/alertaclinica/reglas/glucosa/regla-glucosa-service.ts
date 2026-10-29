import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../../../core/services/api-service';
import {
  ReglaGlucosaResponse,
  ReglaGlucosaRequest,
  ReglaGlucosaFiltros,
} from './regla-glucosa.model';

@Injectable({ providedIn: 'root' })
export class ReglaGlucosaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/reglas/glucosa';

  listar(filtros: ReglaGlucosaFiltros = {}): Observable<ReglaGlucosaResponse[]> {
    let params = new HttpParams();
    if (filtros.estado) params = params.set('estado', filtros.estado);
    if (filtros.sexo) params = params.set('sexo', filtros.sexo);

    return this.http.get<ReglaGlucosaResponse[]>(
      this.api.buildUrl(this.resource),
      { params },
    );
  }

  obtener(id: number): Observable<ReglaGlucosaResponse> {
    return this.http.get<ReglaGlucosaResponse>(
      this.api.buildUrl(`${this.resource}/${id}`),
    );
  }

  crear(regla: ReglaGlucosaRequest): Observable<ReglaGlucosaResponse> {
    return this.http.post<ReglaGlucosaResponse>(
      this.api.buildUrl(this.resource),
      regla,
    );
  }

  actualizar(id: number, regla: ReglaGlucosaRequest): Observable<ReglaGlucosaResponse> {
    return this.http.put<ReglaGlucosaResponse>(
      this.api.buildUrl(`${this.resource}/${id}`),
      regla,
    );
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  reactivar(id: number): Observable<ReglaGlucosaResponse> {
    return this.http.patch<ReglaGlucosaResponse>(
      this.api.buildUrl(`${this.resource}/${id}/reactivar`),
      {},
    );
  }
}
