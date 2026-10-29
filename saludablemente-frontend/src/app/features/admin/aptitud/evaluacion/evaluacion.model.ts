export type EstadoEvaluacion = 'PENDIENTE' | 'COMPLETA' | 'ANULADA';
export type DictamenGlobal = 'APTO' | 'APTO_CON_RESTRICCIONES' | 'NO_APTO';

export interface Evaluacion {
  id?: number;
  idPersona: number;
  idPeriodo: number;
  // Snapshot inmutable
  documentoIdentidadAlMomento?: string;
  nombresCompletosAlMomento?: string;
  edadAlMomento?: number;
  sexoAlMomento?: string;
  areaTrabajoAlMomento?: string;
  // Ciclo de vida
  fechaEvaluacion: string;
  estadoEvaluacion?: EstadoEvaluacion;
  dictamenGlobal?: DictamenGlobal;
  observacionesRestricciones?: string;
  fitnessScore?: number;
  // Detalles anidados
  detalleFuerza?: Fuerza;
  detalleCardio?: Cardio;
}

export interface EvaluacionRequest {
  idPersona: number;
  idPeriodo: number;
  fechaEvaluacion: string;
}

export interface ObservacionesRequest {
  observaciones: string;
}

export interface EvaluacionResumen {
  id: number;
  documentoIdentidadAlMomento: string;
  nombresCompletosAlMomento: string;
  areaTrabajoAlMomento?: string;
  fechaEvaluacion: string;
  estadoEvaluacion: EstadoEvaluacion;
  dictamenGlobal?: DictamenGlobal;
  fitnessScore?: number;
}

export interface EvaluacionFiltros {
  idPeriodo?: number;
  estado?: EstadoEvaluacion;
  area?: string;
  texto?: string;
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

// Import necesario abajo (en el mismo archivo, no puede ir arriba)
import { Fuerza } from '../fuerza/fuerza.model';
import { Cardio } from '../cardio/cardio.model';
