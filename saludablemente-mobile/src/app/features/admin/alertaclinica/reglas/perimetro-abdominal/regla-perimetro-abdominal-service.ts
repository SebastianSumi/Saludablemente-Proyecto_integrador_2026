import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../../../core/services/api-service';
import {
  ReglaPerimetroAbdominalResponse,
  ReglaPerimetroAbdominalRequest,
  ReglaPerimetroAbdominalFiltros,
} from './regla-perimetro-abdominal.model';

@Injectable({ providedIn: 'root' })
export class ReglaPerimetroAbdominalService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/reglas/perimetro-abdominal';

  listar(filtros: ReglaPerimetroAbdominalFiltros = {}): Observable<ReglaPerimetroAbdominalResponse[]> {
    let params = new HttpParams();
    if (filtros.estado) params = params.set('estado', filtros.estado);
    if (filtros.sexo) params = params.set('sexo', filtros.sexo);

    return this.http.get<ReglaPerimetroAbdominalResponse[]>(
      this.api.buildUrl(this.resource),
      { params },
    );
  }

  obtener(id: number): Observable<ReglaPerimetroAbdominalResponse> {
    return this.http.get<ReglaPerimetroAbdominalResponse>(
      this.api.buildUrl(`${this.resource}/${id}`),
    );
  }

  crear(regla: ReglaPerimetroAbdominalRequest): Observable<ReglaPerimetroAbdominalResponse> {
    return this.http.post<ReglaPerimetroAbdominalResponse>(
      this.api.buildUrl(this.resource),
      regla,
    );
  }

  actualizar(id: number, regla: ReglaPerimetroAbdominalRequest): Observable<ReglaPerimetroAbdominalResponse> {
    return this.http.put<ReglaPerimetroAbdominalResponse>(
      this.api.buildUrl(`${this.resource}/${id}`),
      regla,
    );
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  reactivar(id: number): Observable<ReglaPerimetroAbdominalResponse> {
    return this.http.patch<ReglaPerimetroAbdominalResponse>(
      this.api.buildUrl(`${this.resource}/${id}/reactivar`),
      {},
    );
  }
}
