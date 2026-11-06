export type ClasificacionGrasaVisceral = 'NORMAL' | 'ALTO' | 'MUY_ALTO';

export type SexoBiologico = 'M' | 'F';
export type EstadoRegla = 'ACTIVO' | 'INACTIVO';

export interface ReglaGrasaVisceralResponse {
  idReglaVisceral: number;
  sexo: SexoBiologico;
  edadMin: number;
  edadMax: number;
  nivelMin: number;
  nivelMax: number;
  puntosRiesgo: number;
  clasificacion: ClasificacionGrasaVisceral;
  estado: EstadoRegla;
  fechaBaja: string | null;
}

export interface ReglaGrasaVisceralRequest {
  sexo: SexoBiologico;
  edadMin: number;
  edadMax: number;
  nivelMin: number;
  nivelMax: number;
  puntosRiesgo: number;
  clasificacion: ClasificacionGrasaVisceral;
}

export interface ReglaGrasaVisceralFiltros {
  estado?: EstadoRegla;
  sexo?: SexoBiologico;
}
