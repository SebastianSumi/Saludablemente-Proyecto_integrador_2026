import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../../core/services/api-service';
import { EstadoPeriodo, Periodo, PeriodoRequest } from './periodo.model';

@Injectable({ providedIn: 'root' })
export class PeriodoService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/periodos';

  listar(estado?: EstadoPeriodo): Observable<Periodo[]> {
    let params = new HttpParams();
    if (estado) {
      params = params.set('estado', estado);
    }
    return this.http.get<Periodo[]>(this.api.buildUrl(this.resource), { params });
  }

  obtener(id: number): Observable<Periodo> {
    return this.http.get<Periodo>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  obtenerVigente(): Observable<Periodo> {
    return this.http.get<Periodo>(this.api.buildUrl(`${this.resource}/vigente`));
  }

  crear(periodo: PeriodoRequest): Observable<Periodo> {
    return this.http.post<Periodo>(this.api.buildUrl(this.resource), periodo);
  }

  actualizar(id: number, periodo: PeriodoRequest): Observable<Periodo> {
    return this.http.put<Periodo>(this.api.buildUrl(`${this.resource}/${id}`), periodo);
  }

  marcarVigente(id: number): Observable<Periodo> {
    return this.http.patch<Periodo>(this.api.buildUrl(`${this.resource}/${id}/vigente`), {});
  }

  cerrar(id: number): Observable<Periodo> {
    return this.http.patch<Periodo>(this.api.buildUrl(`${this.resource}/${id}/cerrar`), {});
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`${this.resource}/${id}`));
  }
}
