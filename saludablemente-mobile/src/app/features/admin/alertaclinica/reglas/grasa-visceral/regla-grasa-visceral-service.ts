import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../../../core/services/api-service';
import {
  ReglaGrasaVisceralResponse,
  ReglaGrasaVisceralRequest,
  ReglaGrasaVisceralFiltros,
} from './regla-grasa-visceral.model';

@Injectable({ providedIn: 'root' })
export class ReglaGrasaVisceralService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/reglas/grasa-visceral';

  listar(filtros: ReglaGrasaVisceralFiltros = {}): Observable<ReglaGrasaVisceralResponse[]> {
    let params = new HttpParams();
    if (filtros.estado) params = params.set('estado', filtros.estado);
    if (filtros.sexo) params = params.set('sexo', filtros.sexo);

    return this.http.get<ReglaGrasaVisceralResponse[]>(
      this.api.buildUrl(this.resource),
      { params },
    );
  }

  obtener(id: number): Observable<ReglaGrasaVisceralResponse> {
    return this.http.get<ReglaGrasaVisceralResponse>(
      this.api.buildUrl(`${this.resource}/${id}`),
    );
  }

  crear(regla: ReglaGrasaVisceralRequest): Observable<ReglaGrasaVisceralResponse> {
    return this.http.post<ReglaGrasaVisceralResponse>(
      this.api.buildUrl(this.resource),
      regla,
    );
  }

  actualizar(id: number, regla: ReglaGrasaVisceralRequest): Observable<ReglaGrasaVisceralResponse> {
    return this.http.put<ReglaGrasaVisceralResponse>(
      this.api.buildUrl(`${this.resource}/${id}`),
      regla,
    );
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  reactivar(id: number): Observable<ReglaGrasaVisceralResponse> {
    return this.http.patch<ReglaGrasaVisceralResponse>(
      this.api.buildUrl(`${this.resource}/${id}/reactivar`),
      {},
    );
  }
}
