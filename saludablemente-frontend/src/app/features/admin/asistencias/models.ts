// Modelos del módulo Asistencias. Réplica fiel de los DTOs del backend
// (SaludabementeERP :: asistencias).

export type EstadoAsistencia = 'ASISTIO' | 'AUSENTE' | 'EXONERADO' | 'INCOMPLETA' | 'TARDE';
export type MarcaAsistencia = 'PRESENTE' | 'TARDE' | 'FALTA';
export type EstadoCuarentena = 'PENDIENTE' | 'APROBADO' | 'RECHAZADO';
export type EstadoJustificacion = 'PENDIENTE' | 'APROBADO' | 'RECHAZADO';
export type MotivoCuarentena =
  | 'FUERA_GEOCERCA'
  | 'QR_EXPIRADO'
  | 'SYNC_TARDIA'
  | 'PERSONA_INACTIVA'
  | 'DUPLICADO'
  | 'ACTIVIDAD_CERRADA';
export type MetodoRegistro =
  | 'MANUAL'
  | 'QR_COORDINADOR'
  | 'QR_AUTOSERVICIO'
  | 'OFFLINE_QR_COORDINADOR'
  | 'OFFLINE_QR_AUTOSERVICIO'
  | 'SISTEMA_CIERRE';
export type TipoAsistencia = 'CHECK_IN' | 'CHECK_OUT' | 'SIMPLE';

// ---- Registro ----

export interface Asistencia {
  idAsistencia: number;
  idLocal?: string;
  idActividad: number;
  idPersona: number;
  tipoAsistencia: TipoAsistencia;
  metodoRegistro: MetodoRegistro;
  escaneadoPor?: string;
  estado: EstadoAsistencia;
  horaMarcado?: string;
  horaCheckIn?: string;
  horaCheckOut?: string;
  latitud?: number;
  longitud?: number;
  distanciaGeocercaM?: number;
  flagSospechoso?: boolean;
  syncStatus?: string;
  idUsuarioRegistro?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface AsistenciaManualRequest {
  idActividad: number;
  idPersona: number;
  tipoAsistencia: TipoAsistencia;
  latitud?: number;
  longitud?: number;
  observacion?: string;
}

export interface AsistenciaManualDetalleRequest {
  idActividad: number;
  tipoAsistencia: TipoAsistencia;
  latitud?: number;
  longitud?: number;
  detalle: { idPersona: number; marca: MarcaAsistencia }[];
}

export interface AsistenciaActividad {
  idActividad: number;
  nombreActividad: string;
  detalle: Asistencia[];
}

export interface AsistenciaQrRequest {
  idActividad: number;
  codigoQrPersonal: string;
  codigoQrActividad: string;
  tipoAsistencia: TipoAsistencia;
  latitud?: number;
  longitud?: number;
  dispositivoId?: string;
  idLocal?: string;
}

export interface MarcacionRequest {
  idActividad: number;
  idPersona: number;
  marca: MarcaAsistencia;
  tipoAsistencia: TipoAsistencia;
  latitud?: number;
  longitud?: number;
}

export interface MarcacionLoteRequest {
  idActividad: number;
  idsPersona: number[];
  tipoAsistencia: TipoAsistencia;
  latitud?: number;
  longitud?: number;
}

export interface FalloMarcacion {
  idPersona: number;
  motivo?: string;
}

export interface MarcacionLoteResponse {
  procesados: number;
  creados: number;
  actualizados: number;
  fallos: FalloMarcacion[];
}

export interface AsistenciaHistorial {
  idAsistencia: number;
  idActividad: number;
  nombreActividad: string;
  fechaActividad: string;
  estado: EstadoAsistencia;
  metodoRegistro: MetodoRegistro;
  horaMarcado?: string;
}

export interface CierreAsistencias {
  idActividad: number;
  totalEsperados: number;
  totalAsistentes: number;
  totalAusentes: number;
  totalExonerados: number;
  totalIncompletas: number;
  cierreRealizado: boolean;
}

// ---- Cuarentena ----

export interface Cuarentena {
  idCuarentena: number;
  idLocal?: string;
  idActividad: number;
  idPersona: number;
  tipoAsistencia: TipoAsistencia;
  horaMarcado?: string;
  latitud?: number;
  longitud?: number;
  distanciaGeocercaM?: number;
  motivo: MotivoCuarentena;
  estado: EstadoCuarentena;
  observaciones?: string;
  idUsuarioResuelve?: number;
  fechaCreacion?: string;
  fechaResolucion?: string;
}

export interface ResolverCuarentenaRequest {
  decision: 'APROBAR' | 'RECHAZAR';
  observaciones?: string;
  idUsuarioResuelve: number;
}

export interface CuarentenaFiltros {
  estado?: EstadoCuarentena;
  motivo?: MotivoCuarentena;
  idActividad?: number;
  idPersona?: number;
  desde?: string;
  hasta?: string;
  page?: number;
  size?: number;
}

export interface Pagina<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

// ---- Exoneración ----

export interface Justificacion {
  idJustificacion: number;
  idActividad: number;
  idPersona: number;
  motivo: string;
  urlDocumento?: string;
  estado: EstadoJustificacion;
  observaciones?: string;
  idUsuarioDictamina?: number;
  fechaSolicitud?: string;
  fechaDictamen?: string;
}

export interface JustificacionRequest {
  idActividad: number;
  idPersona: number;
  motivo: string;
  urlDocumento?: string;
}

export interface DictamenRequest {
  estado: 'APROBADO' | 'RECHAZADO';
  observaciones?: string;
  idUsuarioDictamina: number;
}

// ---- QR ----

export interface QrActividad {
  idQrActividad: number;
  idActividad: number;
  version: number;
  codigoQr: string;
  ventanaInicio?: string;
  ventanaFin?: string;
  expiraEnSegundos?: number;
  activo?: boolean;
}

// ---- Sincronización ----

export interface RegistroLote {
  idLocal: string;
  idActividad: number;
  tipoAsistencia: TipoAsistencia;
  horaCaptura: string;
  codigoQrPersonal?: string;
  codigoQrActividad?: string;
  versionQr?: number;
  latitud?: number;
  longitud?: number;
  dispositivoId?: string;
}

export interface LoteRequest {
  dispositivoId: string;
  tipoFlujo: 'COORDINADOR' | 'AUTOSERVICIO';
  registros: RegistroLote[];
}

export interface ResultadoRegistro {
  idLocal: string;
  resultado: 'SYNCED' | 'CUARENTENA' | 'FAILED';
  motivo?: string;
  idAsistencia?: number;
  idCuarentena?: number;
}

export interface LoteResponse {
  procesados: number;
  synced: number;
  cuarentena: number;
  failed: number;
  resultados: ResultadoRegistro[];
}

// ---- Reportes ----

export interface Banda {
  codigo: string;
  etiqueta: string;
  color: string;
  emoji: string;
}

export interface Participacion {
  idActividad: number;
  totalEsperados: number;
  totalAsistentes: number;
  totalAusentes: number;
  totalExonerados: number;
  totalIncompletas: number;
  totalCuarentenaPendiente: number;
  porcentajeParticipacion: number;
  banda?: Banda;
  metodoMasUsado?: string;
}

export interface Ausentismo {
  minRacha: number;
  total: number;
  idsPersona: number[];
}

// ---- Agenda (selector de actividades). Réplica de ActividadResponse ----

export interface Actividad {
  idActividad: number;
  nombre: string;
  descripcion?: string;
  fechaActividad?: string;
  horaInicio?: string;
  horaFin?: string;
  lugar?: string;
  estado?: string;
  requiereCheckInOut?: boolean;
  duracionMinutos?: number;
}
