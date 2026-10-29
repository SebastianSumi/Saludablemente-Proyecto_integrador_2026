import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../../core/services/api-service';
import { Area, AreaRequest } from './area.model';

@Injectable({ providedIn: 'root' })
export class AreaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/areas';

  listar(activo?: boolean): Observable<Area[]> {
    let params = new HttpParams();
    if (activo !== undefined && activo !== null) {
      params = params.set('activo', String(activo));
    }
    return this.http.get<Area[]>(this.api.buildUrl(this.resource), { params });
  }

  obtener(id: number): Observable<Area> {
    return this.http.get<Area>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  crear(request: AreaRequest): Observable<Area> {
    return this.http.post<Area>(this.api.buildUrl(this.resource), request);
  }

  actualizar(id: number, request: AreaRequest): Observable<Area> {
    return this.http.put<Area>(this.api.buildUrl(`${this.resource}/${id}`), request);
  }

  inactivar(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`${this.resource}/${id}`));
  }
}
