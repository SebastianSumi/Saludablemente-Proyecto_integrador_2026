// Modelos del módulo Metas. Réplica fiel de los DTOs del backend
// (SaludabementeERP :: metas). Solo lectura: no tocar backend.

export type EstadoMeta = 'ACTIVA' | 'COMPLETADA' | 'DESVIADA' | 'ABANDONADA';
export type ModalidadMeta = 'CUANTITATIVA' | 'CUALITATIVA';
export type TipoCalculo = 'ULTIMO_VALOR' | 'ACUMULADO';
export type TipoOrigen = 'PERSONAL' | 'CLINICA';
export type PilarBienestar =
  | 'ELECCION'
  | 'DESCANSO'
  | 'MEDIO_AMBIENTE'
  | 'ACTIVIDAD'
  | 'CONFIANZA'
  | 'RELACIONES_INTERPERSONALES'
  | 'PERSPECTIVA'
  | 'NUTRICION';

export interface Meta {
  id: number;
  idPersona: number;
  idEvaluacionOrigen?: number;
  idAlertaOrigen?: number;
  idProfesionalPrescriptor?: number;
  pilar: PilarBienestar;
  modalidad: ModalidadMeta;
  tipoOrigen: TipoOrigen;
  tipoCalculo: TipoCalculo;
  indicador: string;
  unidad?: string;
  descripcion: string;
  valorInicial: number;
  valorObjetivo: number;
  valorActual: number;
  fechaInicio: string;
  fechaObjetivo: string;
  fechaCierre?: string;
  estado: EstadoMeta;
}

export interface ProgresoMeta {
  idMeta: number;
  idPersona: number;
  descripcion: string;
  pilar: PilarBienestar;
  modalidad: ModalidadMeta;
  tipoOrigen: TipoOrigen;
  estado: EstadoMeta;
  indicador: string;
  unidad?: string;
  valorInicial: number;
  valorObjetivo: number;
  valorActual: number;
  porcentajeAvance: number;
  rachaActual: number;
  totalCheckIns: number;
  fechaInicio: string;
  fechaObjetivo: string;
  ultimoCheckIn?: string;
  desviada: boolean;
}

export interface MetaRequest {
  idPersona: number;
  pilar: PilarBienestar;
  modalidad: ModalidadMeta;
  tipoCalculo: TipoCalculo;
  descripcion: string;
  indicador: string;
  unidad?: string;
  valorInicial: number;
  valorObjetivo: number;
  fechaInicio: string;
  fechaObjetivo: string;
}

export interface MetaClinicaRequest extends MetaRequest {
  idEvaluacionOrigen: number;
  idAlertaOrigen: number;
  idProfesionalPrescriptor: number;
}

export interface MetaSugeridaRequest {
  idPersona: number;
  tipoMeta: string;
  descripcion: string;
  valorInicial: number;
  valorObjetivo: number;
  fechaLimite: string;
  pilar: PilarBienestar;
}

export interface CheckInRequest {
  idMeta: number;
  idPersona: number;
  fecha?: string;
  valorReportado?: number;
  logrado?: boolean;
  nota?: string;
  evidenciaUrl?: string;
}
