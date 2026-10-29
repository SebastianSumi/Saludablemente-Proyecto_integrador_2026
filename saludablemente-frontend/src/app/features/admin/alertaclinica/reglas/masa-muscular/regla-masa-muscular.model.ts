export type ClasificacionMasaMuscular =
  | 'BAJO'
  | 'NORMAL'
  | 'ELEVADO'
  | 'MUY_ELEVADO';

export type SexoBiologico = 'M' | 'F';
export type EstadoRegla = 'ACTIVO' | 'INACTIVO';

export interface ReglaMasaMuscularResponse {
  idReglaMusculo: number;
  sexo: SexoBiologico;
  edadMin: number;
  edadMax: number;
  porcentajeMin: number;
  porcentajeMax: number;
  puntosRiesgo: number;
  clasificacion: ClasificacionMasaMuscular;
  estado: EstadoRegla;
  fechaBaja: string | null;
}

export interface ReglaMasaMuscularRequest {
  sexo: SexoBiologico;
  edadMin: number;
  edadMax: number;
  porcentajeMin: number;
  porcentajeMax: number;
  puntosRiesgo: number;
  clasificacion: ClasificacionMasaMuscular;
}

export interface ReglaMasaMuscularFiltros {
  estado?: EstadoRegla;
  sexo?: SexoBiologico;
}
