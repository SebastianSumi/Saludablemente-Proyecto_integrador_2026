import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api-service';
import { Noticia, NoticiaFeed, NoticiaFiltros, NoticiaRequest, Pagina } from './models';

@Injectable({ providedIn: 'root' })
export class NoticiaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly admin = '/api/v1/noticias/admin';

  listar(filtros: NoticiaFiltros = {}): Observable<Pagina<Noticia>> {
    let params = new HttpParams();
    if (filtros.estado) params = params.set('estado', filtros.estado);
    if (filtros.idCategoria) params = params.set('idCategoria', String(filtros.idCategoria));
    if (filtros.idArea) params = params.set('idArea', String(filtros.idArea));
    if (filtros.idTeam) params = params.set('idTeam', String(filtros.idTeam));
    params = params.set('page', String(filtros.page ?? 0));
    params = params.set('size', String(filtros.size ?? 20));
    return this.http.get<Pagina<Noticia>>(this.api.buildUrl(this.admin), { params });
  }

  obtener(id: number): Observable<Noticia> {
    return this.http.get<Noticia>(this.api.buildUrl(`${this.admin}/${id}`));
  }

  crear(request: NoticiaRequest): Observable<Noticia> {
    return this.http.post<Noticia>(this.api.buildUrl(this.admin), request);
  }

  actualizar(id: number, request: NoticiaRequest): Observable<Noticia> {
    return this.http.put<Noticia>(this.api.buildUrl(`${this.admin}/${id}`), request);
  }

  subirImagen(id: number, archivo: File): Observable<Noticia> {
    const datos = new FormData();
    datos.append('imagen', archivo);
    return this.http.post<Noticia>(
      this.api.buildUrl(`${this.admin}/${id}/imagen`),
      datos
    );
  }

  eliminarImagen(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`${this.admin}/${id}/imagen`));
  }

  publicar(id: number): Observable<Noticia> {
    return this.http.patch<Noticia>(
      this.api.buildUrl(`${this.admin}/${id}/publicar`),
      null
    );
  }

  despublicar(id: number): Observable<Noticia> {
    return this.http.patch<Noticia>(
      this.api.buildUrl(`${this.admin}/${id}/despublicar`),
      null
    );
  }

  archivar(id: number): Observable<Noticia> {
    return this.http.patch<Noticia>(
      this.api.buildUrl(`${this.admin}/${id}/archivar`),
      null
    );
  }
}

@Injectable({ providedIn: 'root' })
export class NoticiaPublicaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/noticias';

  feed(filtros: { idCategoria?: number; idPersona?: number; page?: number; size?: number } = {}): Observable<Pagina<NoticiaFeed>> {
    let params = new HttpParams();
    if (filtros.idCategoria) params = params.set('idCategoria', String(filtros.idCategoria));
    if (filtros.idPersona) params = params.set('idPersona', String(filtros.idPersona));
    params = params.set('page', String(filtros.page ?? 0));
    params = params.set('size', String(filtros.size ?? 20));
    return this.http.get<Pagina<NoticiaFeed>>(this.api.buildUrl(`${this.resource}/feed`), {
      params,
    });
  }

  detalle(id: number, idPersona?: number): Observable<Noticia> {
    let params = new HttpParams();
    if (idPersona) params = params.set('idPersona', String(idPersona));
    return this.http.get<Noticia>(this.api.buildUrl(`${this.resource}/${id}`), { params });
  }
}
