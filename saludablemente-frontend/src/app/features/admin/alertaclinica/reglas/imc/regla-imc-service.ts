import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../../../core/services/api-service';
import {
  ReglaImcResponse,
  ReglaImcRequest,
  ReglaImcFiltros,
} from './regla-imc.model';

@Injectable({ providedIn: 'root' })
export class ReglaImcService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/reglas/imc';

  listar(filtros: ReglaImcFiltros = {}): Observable<ReglaImcResponse[]> {
    let params = new HttpParams();
    if (filtros.estado) params = params.set('estado', filtros.estado);
    if (filtros.sexo) params = params.set('sexo', filtros.sexo);

    return this.http.get<ReglaImcResponse[]>(
      this.api.buildUrl(this.resource),
      { params },
    );
  }

  obtener(id: number): Observable<ReglaImcResponse> {
    return this.http.get<ReglaImcResponse>(
      this.api.buildUrl(`${this.resource}/${id}`),
    );
  }

  crear(regla: ReglaImcRequest): Observable<ReglaImcResponse> {
    return this.http.post<ReglaImcResponse>(
      this.api.buildUrl(this.resource),
      regla,
    );
  }

  actualizar(id: number, regla: ReglaImcRequest): Observable<ReglaImcResponse> {
    return this.http.put<ReglaImcResponse>(
      this.api.buildUrl(`${this.resource}/${id}`),
      regla,
    );
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  reactivar(id: number): Observable<ReglaImcResponse> {
    return this.http.patch<ReglaImcResponse>(
      this.api.buildUrl(`${this.resource}/${id}/reactivar`),
      {},
    );
  }
}
