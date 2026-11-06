import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api-service';
import {
  PasswordRequest,
  RolesRequest,
  Usuario,
  UsuarioFiltros,
  UsuarioRequest,
  Pagina,
} from './models';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/usuarios';

  crear(request: UsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(this.api.buildUrl(this.resource), request);
  }

  buscar(filtros: UsuarioFiltros = {}): Observable<Pagina<Usuario>> {
    let params = new HttpParams();
    if (filtros.email) params = params.set('email', filtros.email);
    if (filtros.activo !== undefined && filtros.activo !== null) {
      params = params.set('activo', String(filtros.activo));
    }
    params = params.set('page', String(filtros.page ?? 0));
    params = params.set('size', String(filtros.size ?? 20));
    return this.http.get<Pagina<Usuario>>(this.api.buildUrl(this.resource), { params });
  }

  obtener(id: number): Observable<Usuario> {
    return this.http.get<Usuario>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  actualizarRoles(id: number, request: RolesRequest): Observable<Usuario> {
    return this.http.put<Usuario>(
      this.api.buildUrl(`${this.resource}/${id}/roles`),
      request
    );
  }

  actualizarPassword(id: number, request: PasswordRequest): Observable<void> {
    return this.http.put<void>(
      this.api.buildUrl(`${this.resource}/${id}/password`),
      request
    );
  }

  inactivar(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  reactivar(id: number): Observable<void> {
    return this.http.post<void>(
      this.api.buildUrl(`${this.resource}/${id}/reactivar`),
      null
    );
  }
}
