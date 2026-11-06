import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../../../core/services/api-service';
import {
  ReglaColesterolResponse,
  ReglaColesterolRequest,
  ReglaColesterolFiltros,
} from './regla-colesterol.model';

@Injectable({ providedIn: 'root' })
export class ReglaColesterolService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/reglas/colesterol';

  listar(filtros: ReglaColesterolFiltros = {}): Observable<ReglaColesterolResponse[]> {
    let params = new HttpParams();
    if (filtros.estado) params = params.set('estado', filtros.estado);
    if (filtros.sexo) params = params.set('sexo', filtros.sexo);

    return this.http.get<ReglaColesterolResponse[]>(
      this.api.buildUrl(this.resource),
      { params },
    );
  }

  obtener(id: number): Observable<ReglaColesterolResponse> {
    return this.http.get<ReglaColesterolResponse>(
      this.api.buildUrl(`${this.resource}/${id}`),
    );
  }

  crear(regla: ReglaColesterolRequest): Observable<ReglaColesterolResponse> {
    return this.http.post<ReglaColesterolResponse>(
      this.api.buildUrl(this.resource),
      regla,
    );
  }

  actualizar(id: number, regla: ReglaColesterolRequest): Observable<ReglaColesterolResponse> {
    return this.http.put<ReglaColesterolResponse>(
      this.api.buildUrl(`${this.resource}/${id}`),
      regla,
    );
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  reactivar(id: number): Observable<ReglaColesterolResponse> {
    return this.http.patch<ReglaColesterolResponse>(
      this.api.buildUrl(`${this.resource}/${id}/reactivar`),
      {},
    );
  }
}
