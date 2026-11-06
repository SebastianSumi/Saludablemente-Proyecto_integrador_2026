import { AntropometricoResponse } from '../antropometrico/antropometrico.model';
import { BioquimicoResponse } from '../bioquimico/bioquimico.model';

export type EstadoEvaluacion = 'PENDIENTE' | 'COMPLETA' | 'ANULADA';

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface EvaluacionResumen {
  idEvaluacion: number;
  idPersona: number;
  documentoIdentidad: string;
  nombresCompletos: string;
  areaTrabajoAlMomento: string;
  idPeriodo: string;
  fechaEvaluacion: string;
  estadoEvaluacion: EstadoEvaluacion;
}

export interface EvaluacionResponse {
  idEvaluacion: number;
  idPersona: number;
  idPeriodo: string;
  documentoIdentidad: string;
  nombresCompletos: string;
  edadAlMomento: number;
  sexoAlMomento: string;
  areaTrabajoAlMomento: string;
  fechaEvaluacion: string;
  estadoEvaluacion: EstadoEvaluacion;
  observaciones: string | null;
  detalleAntropometrico: AntropometricoResponse | null;
  detalleBioquimico: BioquimicoResponse | null;
}

export interface EvaluacionInitRequest {
  idPersona: number;
  idPeriodoId: number;
}

export interface EvaluacionAgregado {
  total: number;
  promedioImc: number | null;
  promedioPorcentajeGrasa: number | null;
  promedioPorcentajeMasaMuscular: number | null;
}
