import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { ApiService } from '../../core/services/api-service';
import * as S from './social.model';

@Injectable({ providedIn: 'root' })
export class SocialService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private url(path = '') {
    return this.api.buildUrl(`/api/v1/social${path}`);
  }
  private params(values: Record<string, string | number | null | undefined>) {
    let params = new HttpParams();
    for (const [key, value] of Object.entries(values))
      if (value != null) params = params.set(key, String(value));
    return params;
  }
  muro(
    page = 0,
    tab = 'PARA_TI',
    scope: S.Scope = 'GLOBAL',
    id: number | null = null,
    tag: string | null = null,
  ) {
    const path = tag
      ? `/hashtags/${encodeURIComponent(tag)}/muro`
      : scope === 'EQUIPO'
        ? `/muro/equipos/${id}`
        : scope === 'ACTIVIDAD'
          ? `/muro/actividades/${id}`
          : '/muro';
    return this.http.get<S.Muro>(this.url(path), { params: this.params({ page, size: 20, tab }) });
  }
  equipo(id: number) {
    return this.http.get<S.EquipoSocial>(this.url(`/equipos/${id}/perfil`));
  }
  detalle(id: number) {
    return this.http.get<S.PostDetalle>(this.url(`/posts/${id}`));
  }
  crear(body: S.CrearPost) {
    return this.http.post<S.PostDetalle>(this.url('/posts'), body);
  }
  editar(id: number, body: S.EditarPost) {
    return this.http.patch<S.PostDetalle>(this.url(`/posts/${id}`), body);
  }
  eliminar(id: number) {
    return this.http.delete<void>(this.url(`/posts/${id}`));
  }
  compartir(id: number, contenido: string | null, idEquipoAutor: number | null) {
    return this.http.post<{ idPostShare: number; fechaShare: string }>(
      this.url(`/posts/${id}/share`),
      { contenido, idEquipoAutor },
    );
  }
  guardar(id: number, activo: boolean) {
    return activo
      ? this.http.post<void>(this.url(`/posts/${id}/guardar`), {})
      : this.http.delete<void>(this.url(`/posts/${id}/guardar`));
  }
  fijar(id: number, body: S.Fijar) {
    return this.http.post<void>(this.url(`/posts/${id}/fijar`), body);
  }
  desfijar(id: number, scopeTipo: S.Scope, scopeId: number | null) {
    return this.http.delete<void>(this.url(`/posts/${id}/fijar`), {
      params: this.params({ scopeTipo, scopeId }),
    });
  }
  comentarios(id: number, page = 0) {
    return this.http.get<S.Page<S.Comentario>>(this.url(`/posts/${id}/comentarios`), {
      params: this.params({ page, size: 20 }),
    });
  }
  comentario(post: number, id: number) {
    return this.http.get<S.Comentario>(this.url(`/posts/${post}/comentarios/${id}`));
  }
  comentar(id: number, body: S.CrearComentario) {
    return this.http.post<S.Comentario>(this.url(`/posts/${id}/comentarios`), body);
  }
  editarComentario(post: number, id: number, contenido: string) {
    return this.http.patch<S.Comentario>(this.url(`/posts/${post}/comentarios/${id}`), {
      contenido,
    });
  }
  eliminarComentario(post: number, id: number) {
    return this.http.delete<void>(this.url(`/posts/${post}/comentarios/${id}`));
  }
  subirAdjunto(post: number, archivo: File) {
    const body = new FormData();
    body.append('archivo', archivo);
    return this.http.post<S.Adjunto>(this.url(`/posts/${post}/comentarios/adjuntos`), body);
  }
  private reaccionPath(id: number, comentario: boolean) {
    return `/${comentario ? 'comentarios' : 'posts'}/${id}/reacciones`;
  }
  reaccionar(id: number, tipo: S.TipoReaccion | null, comentario = false) {
    const url = this.url(this.reaccionPath(id, comentario));
    return tipo ? this.http.post<void>(url, { tipo }) : this.http.delete<void>(url);
  }
  reacciones(id: number, comentario = false) {
    return this.http.get<S.Reacciones>(this.url(this.reaccionPath(id, comentario)));
  }
  quienReacciono(id: number, comentario = false, tipo: S.TipoReaccion | null = null, page = 0) {
    return this.http.get<S.Page<S.ReaccionDetalle>>(
      this.url(`${this.reaccionPath(id, comentario)}/usuarios`),
      { params: this.params({ tipo, page, size: 20 }) },
    );
  }
  buscar(q: string, tipo: S.TipoBusqueda, page = 0) {
    return this.http.get<S.Busqueda>(this.url('/buscar'), {
      params: this.params({ q, tipo, page, size: 20 }),
    });
  }
  sugerir(tipo: S.TipoMencion, q: string): Observable<S.Sugerencia[]> {
    const params = this.params({ tipo, q, limite: 10 });
    if (tipo === 'USUARIO')
      return this.http.get<S.UsuarioSugerido[]>(this.url('/menciones/buscar'), { params }).pipe(
        map((items) =>
          items.map((x) => ({
            id: x.idUsuario,
            nombre: x.nombreCompleto,
            imagenUrl: x.fotoUrl,
            detalle: 'Persona de la comunidad',
            token: `@${x.username}`,
            tipo,
          })),
        ),
      );
    if (tipo === 'EQUIPO')
      return this.http.get<S.EquipoSugerido[]>(this.url('/menciones/buscar'), { params }).pipe(
        map((items) =>
          items.map((x) => ({
            id: x.idEquipo,
            nombre: x.nombre,
            imagenUrl: x.logoUrl,
            detalle: 'Equipo',
            token: `@equipo{${x.idEquipo}}`,
            tipo,
          })),
        ),
      );
    return this.http.get<S.ActividadSugerida[]>(this.url('/menciones/buscar'), { params }).pipe(
      map((items) =>
        items.map((x) => ({
          id: x.idActividad,
          nombre: x.nombre,
          imagenUrl: x.logoUrl,
          detalle: [x.estado, x.fechaActividad].filter(Boolean).join(' · '),
          token: `@actividad{${x.idActividad}}`,
          tipo,
        })),
      ),
    );
  }
  tendencias() {
    return this.http.get<S.Tendencia[]>(this.url('/hashtags/tendencia'));
  }
  cabeceraPerfil(id: number) {
    return this.http.get<S.PerfilCabecera>(this.url(`/usuarios/${id}/perfil`));
  }
  miPerfilSocial() {
    return this.http.get<S.PerfilCabecera>(this.url('/mi-perfil'));
  }
  companeros(id: number, page = 0) {
    return this.http.get<S.Page<S.Autor>>(this.url(`/usuarios/${id}/companeros`), {
      params: this.params({ page, size: 9 }),
    });
  }
  editarMiPresentacion(presentacion: string) {
    return this.http.patch<S.PerfilCabecera>(this.url('/mi-perfil'), { presentacion });
  }
  subirFotoPerfil(archivo: File, portada = false) {
    const form = new FormData();
    form.append('archivo', archivo);
    return this.http.post<S.PerfilCabecera>(
      this.url(portada ? '/mi-perfil/portada' : '/mi-perfil/foto'),
      form,
    );
  }
  perfil(id: number, page = 0) {
    return this.http.get<S.PerfilSocial>(this.url(`/usuarios/${id}/actividad`), {
      params: this.params({ page, size: 20 }),
    });
  }
  miContenido(tab: S.TabContenido, page = 0) {
    return this.http.get<S.MiContenido>(this.url('/mi-contenido'), {
      params: this.params({ tab, page, size: 20 }),
    });
  }
  reportar(post: number, motivo: S.MotivoReporte, detalle: string | null, comentario?: number) {
    return this.http.post<S.Reporte>(
      this.url(`/posts/${post}${comentario ? `/comentarios/${comentario}` : ''}/reportar`),
      { motivo, detalle },
    );
  }
  reportes(estado: S.EstadoReporte, page = 0) {
    return this.http.get<S.Page<S.Reporte>>(this.url('/admin/reportes'), {
      params: this.params({ estado, page, size: 20 }),
    });
  }
  resolver(id: number, estado: 'ACEPTADO' | 'RECHAZADO', comentario: string | null) {
    return this.http.patch<S.Reporte>(this.url(`/admin/reportes/${id}`), { estado, comentario });
  }
  moderar(id: number, restaurar: boolean, comentario = false) {
    return this.http.patch<void>(
      this.url(
        `/admin/${comentario ? 'comentarios' : 'posts'}/${id}/${restaurar ? 'restaurar' : 'ocultar'}`,
      ),
      {},
    );
  }
  metricas(desde: string, hasta: string) {
    return this.http.get<S.Metricas>(this.url('/admin/metricas'), {
      params: this.params({ desde, hasta }),
    });
  }
}
