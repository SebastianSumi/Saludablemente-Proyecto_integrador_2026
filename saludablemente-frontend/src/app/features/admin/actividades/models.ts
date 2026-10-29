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
 *
 * Tras retirar MinIO solo se admiten URLs absolutas (http/https) o data URIs.
 * Las claves internas se descartan porque ya no hay servidor de archivos.
 */
export function urlImagenActividad(url?: string | null): string | undefined {
  if (!url || url.trim() === '') return undefined;
  const valor = url.trim();
  if (/^https?:\/\//i.test(valor) || valor.startsWith('data:')) return valor;
  return undefined;
}
