export type ClasificacionPorcentajeGrasa =
  | 'BAJO'
  | 'NORMAL'
  | 'ALTO'
  | 'MUY_ALTO';

export type SexoBiologico = 'M' | 'F';
export type EstadoRegla = 'ACTIVO' | 'INACTIVO';

export interface ReglaPorcentajeGrasaResponse {
  idReglaGrasa: number;
  sexo: SexoBiologico;
  edadMin: number;
  edadMax: number;
  porcentajeMin: number;
  porcentajeMax: number;
  puntosRiesgo: number;
  clasificacion: ClasificacionPorcentajeGrasa;
  estado: EstadoRegla;
  fechaBaja: string | null;
}

export interface ReglaPorcentajeGrasaRequest {
  sexo: SexoBiologico;
  edadMin: number;
  edadMax: number;
  porcentajeMin: number;
  porcentajeMax: number;
  puntosRiesgo: number;
  clasificacion: ClasificacionPorcentajeGrasa;
}

export interface ReglaPorcentajeGrasaFiltros {
  estado?: EstadoRegla;
  sexo?: SexoBiologico;
}
