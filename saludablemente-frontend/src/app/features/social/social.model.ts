import { Page } from '../../shared/models/page.model';
export type { Page } from '../../shared/models/page.model';
export type TipoPost = 'MANUAL' | 'ACTIVIDAD' | 'NOTICIA' | 'SHARE';
export type TipoReaccion = 'LIKE' | 'LOVE' | 'HAHA' | 'WOW' | 'SAD' | 'APLAUSO' | 'ENOJADO';
export type Scope = 'GLOBAL' | 'EQUIPO' | 'ACTIVIDAD';
export type TipoMencion = 'USUARIO' | 'ACTIVIDAD' | 'EQUIPO';
export type TipoBusqueda = 'POST' | 'COMENTARIO' | 'AUTOR' | 'HASHTAG';
export type TabContenido = 'PUBLICACIONES' | 'COMENTARIOS' | 'REACCIONES' | 'GUARDADOS';
export type EstadoReporte = 'PENDIENTE' | 'ACEPTADO' | 'RECHAZADO';
export type MotivoReporte = 'SPAM' | 'OFENSIVO' | 'ACOSO' | 'FALSO' | 'OTRO';
export interface Autor {
  idUsuario: number | null;
  username: string | null;
  nombreCompleto: string;
  rolPrincipal: string | null;
  nombreEquipo: string | null;
  idEquipo: number | null;
  fotoUrl: string | null;
}
export interface EquipoSocial {
  idEquipo: number;
  nombre: string;
  descripcion: string | null;
  imagenUrl: string | null;
  color: string | null;
  slogan: string | null;
  referenciaBiblica: string | null;
  textoVersiculo: string | null;
  idUsuarioLider: number | null;
  lider: {
    idUsuario: number;
    username: string;
    nombreCompleto: string;
    fotoUrl: string | null;
    activo: boolean;
  } | null;
  activo: boolean;
  totalMiembros: number;
}
export interface Mencion {
  tipoMencion: TipoMencion;
  refId: number;
  displayName: string | null;
  avatarUrl: string | null;
  url: string | null;
}
export interface Reacciones {
  total: number;
  porTipo: Partial<Record<TipoReaccion, number>>;
  reaccionDelUsuario: TipoReaccion | null;
}
export interface ReaccionDetalle {
  idUsuario: number;
  autor: Autor;
  tipo: TipoReaccion;
  fechaReaccion: string;
}
export interface PostResumen {
  idPost: number;
  tipoContenido: TipoPost;
  refId: number | null;
  idEquipoAutor: number | null;
  titulo: string | null;
  resumen: string | null;
  lugar: string | null;
  fechaEvento: string | null;
  horaInicio: string | null;
  horaFin: string | null;
  estado: string | null;
  imagenUrl: string | null;
  coordinadorNombre: string | null;
  esOficial: boolean;
  fechaPublicacion: string;
  autor: Autor;
  autorEquipo: Autor | null;
  totalComentarios: number;
  reaccionesPorTipo: Partial<Record<TipoReaccion, number>>;
  totalReacciones: number;
  totalShares: number;
  reaccionDelUsuario: TipoReaccion | null;
  guardadoPorMi: boolean;
  postOriginalId: number | null;
  postOriginal: PostResumen | null;
  equipo: EquipoSocial | null;
  puedoEditar: boolean;
  puedoEliminar: boolean;
}
export interface PostDetalle extends Omit<
  PostResumen,
  | 'reaccionesPorTipo'
  | 'totalReacciones'
  | 'reaccionDelUsuario'
  | 'guardadoPorMi'
  | 'postOriginalId'
> {
  imagenes: string[];
  fechaActualizacion: string | null;
  alcanceTotal: number | null;
  comentarios: Comentario[];
  reacciones: Reacciones;
  menciones: Mencion[];
  hashtags: string[];
  guardadoPorMi: boolean;
}
export interface Comentario {
  idComentario: number;
  idPost: number;
  eliminado: boolean;
  idComentarioPadre: number | null;
  idUsuarioAutor: number;
  autor: Autor;
  contenido: string;
  editado: boolean;
  fechaEdicion: string | null;
  fechaComentario: string;
  adjuntosUrls: string[];
  menciones: Mencion[];
  hashtags: string[];
  reacciones: Reacciones;
  respuestas: Comentario[];
  puedoEditar: boolean;
  puedoEliminar: boolean;
}
export interface CrearPost {
  titulo: string | null;
  resumen: string | null;
  idEquipoAutor: number | null;
  imagenesUrls: string[];
}
export type EditarPost = Omit<CrearPost, 'idEquipoAutor'>;
export interface CrearComentario {
  contenido: string;
  idComentarioPadre: number | null;
  adjuntosUrls: string[];
}
export interface Fijar {
  scopeTipo: Scope;
  scopeId: number | null;
  fechaExpiracion: string | null;
}
export interface Muro {
  pinned: PostResumen[];
  posts: Page<PostResumen>;
}
export interface PerfilSocial {
  autor: Autor;
  publicaciones: Page<PostResumen>;
  comentarios: Page<Comentario>;
}
export interface MiContenido {
  publicaciones: Page<PostResumen>;
  comentarios: Page<Comentario>;
  reacciones: Page<PostResumen>;
  guardados: Page<PostResumen>;
}
export interface Busqueda {
  tipo: TipoBusqueda;
  publicaciones: Page<PostResumen>;
  comentarios: Page<Comentario>;
  autores: Autor[];
  hashtags: string[];
}
export interface Adjunto {
  objectKey: string;
  url: string;
  mime: string;
  sizeBytes: number;
}
export interface Reporte {
  idReporte: number;
  idPost: number | null;
  idComentario: number | null;
  idPostContenido: number;
  reportante: Autor;
  motivo: MotivoReporte;
  detalle: string | null;
  fechaReporte: string;
  estado: EstadoReporte;
  revisor: Autor | null;
  fechaRevision: string | null;
  accionTomada: string | null;
  comentarioRevision: string | null;
}
export interface Tendencia {
  tag: string;
  total: number;
}
export interface TopPost {
  idPost: number;
  titulo: string | null;
  total: number;
}
export interface Metricas {
  totalPosts: number;
  totalComentarios: number;
  totalReacciones: number;
  totalShares: number;
  usuariosActivos: number;
  engagement: number;
  topComentados: TopPost[];
  topReaccionados: TopPost[];
  topUsuarios: { idUsuario: number; autor: Autor; total: number }[];
  hashtagsTendencia: Tendencia[];
  volumenPorDia: {
    fecha: string;
    publicaciones: number;
    comentarios: number;
    reacciones: number;
  }[];
}
export interface UsuarioSugerido {
  idUsuario: number;
  username: string;
  nombreCompleto: string;
  fotoUrl: string | null;
}
export interface EquipoSugerido {
  idEquipo: number;
  nombre: string;
  logoUrl: string | null;
}
export interface ActividadSugerida {
  idActividad: number;
  nombre: string;
  estado: string;
  fechaActividad: string;
}
export interface Sugerencia {
  id: number;
  nombre: string;
  token: string;
  tipo: TipoMencion;
}
