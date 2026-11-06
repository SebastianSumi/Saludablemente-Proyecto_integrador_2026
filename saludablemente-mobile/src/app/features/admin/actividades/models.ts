import { environment } from '../../../../environments/environment';

export interface ActividadImagen {
  idImagen: number;
  url: string;
  descripcion?: string;
  orden?: number;
}

export interface Actividad {
  idActividad: number;
  nombre: string;
  descripcion?: string;
  fechaActividad: string;
  horaInicio: string;
  horaFin: string;
  lugar: string;
  estado: string;
  puntajeSalud?: number;
  periodo?: string;
  idUsuarioCreador?: number;
  nombreCreador?: string;
  emailCreador?: string;
  rolCreador?: string;
  fechaCreacion?: string;
  idUsuarioCoordinador?: number;
  nombreCoordinador?: string;
  latitudOficial?: number;
  longitudOficial?: number;
  toleranciaGeocercaMetros?: number;
  requiereCheckInOut?: boolean;
  duracionMinutos?: number;
  permiteAutoservicio?: boolean;
  permiteEscaneoCoordinador?: boolean;
  materiales?: string[];
  inscritos?: InscritoDetalle[];
  totalInscritos?: number;
  logoUrl?: string;
  imagenes?: ActividadImagen[];
}

export interface ActividadRequest {
  nombre: string;
  descripcion?: string;
  fechaActividad: string;
  horaInicio: string;
  horaFin: string;
  lugar: string;
  idUsuarioCreador: number;
  idUsuarioCoordinador: number;
  latitudOficial?: number;
  longitudOficial?: number;
  toleranciaGeocercaMetros?: number;
  requiereCheckInOut?: boolean;
  duracionMinutos?: number;
  periodo?: string;
  permiteAutoservicio?: boolean;
  permiteEscaneoCoordinador?: boolean;
}

export interface InscritoDetalle {
  idPersona: number;
  nombreCompleto: string;
  nombres?: string;
  apellidoPaterno?: string;
  apellidoMaterno?: string;
  areaTrabajo?: string;
  celular?: string;
  sexo?: string;
  activo?: boolean;
  inscritoPorTodos?: boolean;
  fechaInscripcion?: string;
}

export interface Configuracion {
  idActividad: number;
  puntajeSalud?: number;
  materiales: string[];
}

export interface ReporteActividad {
  idActividad: number;
  nombre: string;
  descripcion?: string;
  fechaActividad: string;
  horaInicio: string;
  horaFin: string;
  lugar: string;
  estado: string;
  puntajeSalud?: number;
  nombreCoordinador?: string;
  materiales: string[];
}

export interface ActividadItem {
  idActividad: number;
  nombre: string;
  fecha: string;
  horaInicio: string;
  horaFin: string;
  lugar: string;
  estado: string;
  puntajeSalud?: number;
  nombreCoordinador?: string;
}

export interface ReportePeriodo {
  periodo?: string;
  fechaInicio: string;
  fechaFin: string;
  totalActividades: number;
  totalProgramadas: number;
  totalEnCurso: number;
  totalFinalizadas: number;
  totalCanceladas: number;
  actividades?: ActividadItem[];
}

/**
 * Resuelve la URL pública para mostrar el logo o imagen de actividad.
 * Maneja URLs absolutas de MinIO, URLs relativas y nombres de objeto.
 */
export function urlImagenActividad(url?: string | null): string | undefined {
  if (!url || url.trim() === '') return undefined;
  const valor = url.trim();
  if (/^https?:\/\//i.test(valor) || valor.startsWith('data:')) {
    try {
      const parsed = new URL(valor);
      const configuredMinio = new URL(environment.minio.publicUrl);
      if (parsed.pathname.includes(`/${environment.minio.bucketNoticias}/`)) {
        return `${environment.minio.publicUrl.replace(/\/+$/, '')}${parsed.pathname}`;
      }
    } catch {
      // not a full url
    }
    return valor;
  }
  const base = environment.minio.publicUrl.replace(/\/+$/, '');
  const objeto = valor.replace(/^\/+/, '');
  const conPrefijo = objeto.includes('/') ? objeto : `actividades/${objeto}`;
  return `${base}/${environment.minio.bucketNoticias}/${conPrefijo}`;
}
