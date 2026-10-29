// Modelos del módulo Noticias. Réplica fiel de los DTOs del backend
// (SaludabementeERP :: noticias). Solo lectura: no tocar backend.

export type EstadoNoticia = 'BORRADOR' | 'PUBLICADO' | 'ARCHIVADO';

export interface Categoria {
  id: number;
  nombre: string;
  descripcion?: string;
  colorHex?: string;
  icono?: string;
  orden?: number;
  esSistema?: boolean;
  activo?: boolean;
  cantidadNoticias: number;
}

export interface CategoriaRequest {
  nombre: string;
  descripcion?: string;
  colorHex?: string;
  icono?: string;
  orden?: number;
}

export interface Noticia {
  id: number;
  titulo: string;
  subtitulo?: string;
  contenido: string;
  imagenUrl?: string;
  categoria?: Categoria;
  area?: { id: number; nombre: string };
  teams?: { id: number; nombre: string }[];
  nombreAutor?: string;
  estado: string;
  fechaPublicacion?: string;
  fechaCreacion?: string;
  fechaActualizacion?: string;
}

export interface NoticiaRequest {
  titulo: string;
  subtitulo?: string;
  contenido: string;
  idCategoria: number;
  idArea?: number;
  idsTeams?: number[];
  idUsuarioAutor: number;
}

export interface NoticiaFeed {
  id: number;
  titulo: string;
  subtitulo?: string;
  nombreCategoria?: string;
  colorCategoria?: string;
  imagenUrl?: string;
  fechaPublicacion?: string;
}

export interface Pagina<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface NoticiaFiltros {
  estado?: EstadoNoticia;
  idCategoria?: number;
  idArea?: number;
  idTeam?: number;
  page?: number;
  size?: number;
}

/**
 * Resuelve la URL visible de una portada.
 *
 * Tras retirar MinIO solo se pueden mostrar URLs absolutas (http/https) o
 * data URIs. Las claves internas ya no tienen servidor de archivos y se
 * descartan para evitar peticiones rotas al frontend.
 */
export function urlImagenNoticia(imagenUrl?: string | null): string | undefined {
  if (!imagenUrl || imagenUrl.trim() === '') return undefined;
  const valor = imagenUrl.trim();
  if (/^https?:\/\//i.test(valor) || valor.startsWith('data:')) return valor;
  return undefined;
}
