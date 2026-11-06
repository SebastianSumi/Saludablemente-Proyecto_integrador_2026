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

import { environment } from '../../../../environments/environment';

/**
 * Resuelve la URL visible de una portada. El backend puede guardar la URL
 * pública completa, solo el nombre del objeto, o (bug actual) una URL con
 * prefijo duplicado: en todos los casos devuelve una URL absoluta navegable.
 * Sin esto, un nombre suelto se pide al frontend (puerto 4200) y da 404, y
 * una URL duplicada apunta a un objeto inexistente aunque el archivo sí
 * esté en MinIO.
 */
export function urlImagenNoticia(imagenUrl?: string | null): string | undefined {
  if (!imagenUrl || imagenUrl.trim() === '') return undefined;
  let valor = imagenUrl.trim();
  const base = environment.minio.publicUrl.replace(/\/+$/, '');
  const bucket = environment.minio.bucketNoticias.replace(/^\/+|\/+$/g, '');

  // Caso URL con prefijo duplicado (ej: .../bucket/http://.../bucket/uuid.jpg):
  // quedarse con el último http para rescatar el objeto real.
  const lastHttp = valor.toLowerCase().lastIndexOf('http');
  if (lastHttp > 0) {
    valor = valor.slice(lastHttp).trim();
  }

  if (/^https?:\/\//i.test(valor)) {
    const sinQuery = valor.split('?')[0].split('#')[0];
    const idxBucket = sinQuery.indexOf(`${bucket}/`);
    if (idxBucket >= 0) {
      const objeto = sinQuery.slice(idxBucket + bucket.length + 1).replace(/^\/+/, '');
      if (objeto && !objeto.includes('://')) {
        return `${base}/${bucket}/${objeto}`;
      }
    }
    return sinQuery;
  }

  // Nombre suelto: puede venir como "uuid.jpg", "/uuid.jpg",
  // "bucket/uuid.jpg" o "/bucket/uuid.jpg". Evitar duplicar el bucket.
  let objeto = valor.split('?')[0].split('#')[0].replace(/^\/+/, '');
  if (objeto === bucket || objeto.startsWith(`${bucket}/`)) {
    objeto = objeto.slice(bucket.length).replace(/^\/+/, '');
  }
  if (!objeto) return undefined;
  return `${base}/${bucket}/${objeto}`;
}
