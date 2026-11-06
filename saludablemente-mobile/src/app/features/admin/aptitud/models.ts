// Modelos del módulo Aptitud física. Réplica fiel de los DTOs del backend
// (SaludabementeERP :: aptitudfisica). Solo lectura: no tocar backend.

export type EstadoEvaluacion = 'PENDIENTE' | 'COMPLETA' | 'ANULADA';
export type DictamenGlobal = 'APTO' | 'APTO_CON_RESTRICCIONES' | 'NO_APTO';

export interface EvaluacionResumen {
  id: number;
  documentoIdentidadAlMomento?: string;
  nombresCompletosAlMomento?: string;
  areaTrabajoAlMomento?: string;
  fechaEvaluacion: string;
  estadoEvaluacion: string;
  dictamenGlobal?: string;
  fitnessScore?: number;
}

export interface FuerzaDetalle {
  repeticionesAbdominales?: number;
  repeticionesFlexionBrazo?: number;
  saltoLongitudCm?: number;
  dxCore?: string;
  dxTrenSuperior?: string;
  dxTrenInferior?: string;
  dxFuerzaGlobal?: string;
}

export interface CardioDetalle {
  repeticionesSoga30s?: number;
  tiempoCarrera400mSeg?: number;
  dxCardiovascular?: string;
}

export interface EvaluacionAptitud {
  id: number;
  idPersona: number;
  idPeriodo: number;
  documentoIdentidadAlMomento?: string;
  nombresCompletosAlMomento?: string;
  edadAlMomento?: number;
  sexoAlMomento?: string;
  areaTrabajoAlMomento?: string;
  fechaEvaluacion: string;
  estadoEvaluacion: string;
  dictamenGlobal?: string;
  observacionesRestricciones?: string;
  fitnessScore?: number;
  detalleFuerza?: FuerzaDetalle;
  detalleCardio?: CardioDetalle;
}

export interface EvaluacionInitRequest {
  idPersona: number;
  idPeriodo: number;
  fechaEvaluacion: string;
}

export interface FuerzaRequest {
  repeticionesAbdominales: number;
  repeticionesFlexionBrazo: number;
  saltoLongitudCm: number;
}

export interface CardioRequest {
  repeticionesSoga30s: number;
  tiempoCarrera400mSeg: number;
}

export interface AptitudAgregado {
  total: number;
  pendientes: number;
  completas: number;
  anuladas: number;
  aptos: number;
  aptosConRestricciones: number;
  noAptos: number;
}

export interface Pagina<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface EvaluacionFiltros {
  idPeriodo?: number;
  estado?: EstadoEvaluacion;
  area?: string;
  texto?: string;
  page?: number;
  size?: number;
}
