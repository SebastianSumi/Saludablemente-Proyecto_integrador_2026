export type ClasificacionImc =
  | 'DELGADEZ'
  | 'NORMAL'
  | 'SOBREPESO'
  | 'OBESIDAD';

export type SexoBiologico = 'M' | 'F';
export type EstadoRegla = 'ACTIVO' | 'INACTIVO';

export interface ReglaImcResponse {
  idReglaImc: number;
  sexo: SexoBiologico;
  edadMin: number;
  edadMax: number;
  imcMin: number;
  imcMax: number;
  puntosRiesgo: number;
  clasificacion: ClasificacionImc;
  estado: EstadoRegla;
  fechaBaja: string | null;
}

export interface ReglaImcRequest {
  sexo: SexoBiologico;
  edadMin: number;
  edadMax: number;
  imcMin: number;
  imcMax: number;
  puntosRiesgo: number;
  clasificacion: ClasificacionImc;
}

export interface ReglaImcFiltros {
  estado?: EstadoRegla;
  sexo?: SexoBiologico;
}
