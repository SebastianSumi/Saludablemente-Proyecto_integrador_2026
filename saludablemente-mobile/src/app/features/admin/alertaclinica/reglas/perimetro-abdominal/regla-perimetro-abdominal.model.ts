export type ClasificacionPerimetroAbdominal =
  | 'RIESGO_BAJO'
  | 'RIESGO_ALTO'
  | 'RIESGO_MUY_ALTO';

export type SexoBiologico = 'M' | 'F';
export type EstadoRegla = 'ACTIVO' | 'INACTIVO';

export interface ReglaPerimetroAbdominalResponse {
  idReglaPerimetro: number;
  sexo: SexoBiologico;
  edadMin: number;
  edadMax: number;
  perimetroCmMin: number;
  perimetroCmMax: number;
  puntosRiesgo: number;
  clasificacion: ClasificacionPerimetroAbdominal;
  estado: EstadoRegla;
  fechaBaja: string | null;
}

export interface ReglaPerimetroAbdominalRequest {
  sexo: SexoBiologico;
  edadMin: number;
  edadMax: number;
  perimetroCmMin: number;
  perimetroCmMax: number;
  puntosRiesgo: number;
  clasificacion: ClasificacionPerimetroAbdominal;
}

export interface ReglaPerimetroAbdominalFiltros {
  estado?: EstadoRegla;
  sexo?: SexoBiologico;
}
