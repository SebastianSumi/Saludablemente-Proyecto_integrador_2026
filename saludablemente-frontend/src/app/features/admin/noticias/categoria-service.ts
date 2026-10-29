import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api-service';
import { Categoria, CategoriaRequest } from './models';

@Injectable({ providedIn: 'root' })
export class CategoriaNoticiaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/noticias/categorias';

  listarTodas(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.api.buildUrl(this.resource));
  }

  listarActivas(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.api.buildUrl(`${this.resource}/public`));
  }

  crear(request: CategoriaRequest): Observable<Categoria> {
    return this.http.post<Categoria>(this.api.buildUrl(this.resource), request);
  }

  actualizar(id: number, request: CategoriaRequest): Observable<Categoria> {
    return this.http.put<Categoria>(this.api.buildUrl(`${this.resource}/${id}`), request);
  }

  activar(id: number): Observable<Categoria> {
    return this.http.patch<Categoria>(
      this.api.buildUrl(`${this.resource}/${id}/activar`),
      null
    );
  }

  inactivar(id: number): Observable<Categoria> {
    return this.http.patch<Categoria>(
      this.api.buildUrl(`${this.resource}/${id}/inactivar`),
      null
    );
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`${this.resource}/${id}`));
  }
}
