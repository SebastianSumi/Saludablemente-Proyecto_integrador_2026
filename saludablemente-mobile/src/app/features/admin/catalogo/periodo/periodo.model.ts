export type EstadoPeriodo = 'PLANIFICADO' | 'VIGENTE' | 'CERRADO';

export interface Periodo {
  id?: number;
  codigo: string;
  descripcion: string;
  fechaInicio: string;
  fechaFin: string;
  estado?: EstadoPeriodo;
  cerradoEn?: string;
  cerradoPor?: string;
}

export interface PeriodoRequest {
  codigo: string;
  descripcion: string;
  fechaInicio: string;
  fechaFin: string;
}
