import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../../../core/services/api-service';
import {
  ReglaTrigliceridosResponse,
  ReglaTrigliceridosRequest,
  ReglaTrigliceridosFiltros,
} from './regla-trigliceridos.model';

@Injectable({ providedIn: 'root' })
export class ReglaTrigliceridosService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/reglas/trigliceridos';

  listar(filtros: ReglaTrigliceridosFiltros = {}): Observable<ReglaTrigliceridosResponse[]> {
    let params = new HttpParams();
    if (filtros.estado) params = params.set('estado', filtros.estado);
    if (filtros.sexo) params = params.set('sexo', filtros.sexo);

    return this.http.get<ReglaTrigliceridosResponse[]>(
      this.api.buildUrl(this.resource),
      { params },
    );
  }

  obtener(id: number): Observable<ReglaTrigliceridosResponse> {
    return this.http.get<ReglaTrigliceridosResponse>(
      this.api.buildUrl(`${this.resource}/${id}`),
    );
  }

  crear(regla: ReglaTrigliceridosRequest): Observable<ReglaTrigliceridosResponse> {
    return this.http.post<ReglaTrigliceridosResponse>(
      this.api.buildUrl(this.resource),
      regla,
    );
  }

  actualizar(id: number, regla: ReglaTrigliceridosRequest): Observable<ReglaTrigliceridosResponse> {
    return this.http.put<ReglaTrigliceridosResponse>(
      this.api.buildUrl(`${this.resource}/${id}`),
      regla,
    );
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  reactivar(id: number): Observable<ReglaTrigliceridosResponse> {
    return this.http.patch<ReglaTrigliceridosResponse>(
      this.api.buildUrl(`${this.resource}/${id}/reactivar`),
      {},
    );
  }
}
