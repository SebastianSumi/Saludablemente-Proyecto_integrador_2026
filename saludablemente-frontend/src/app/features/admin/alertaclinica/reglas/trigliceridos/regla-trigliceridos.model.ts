export type ClasificacionTrigliceridos = 'NORMAL' | 'LIMITE' | 'ALTO' | 'MUY_ALTO';

export type SexoBiologico = 'M' | 'F';
export type EstadoRegla = 'ACTIVO' | 'INACTIVO';

export interface ReglaTrigliceridosResponse {
  idReglaTrigliceridos: number;
  sexo: SexoBiologico;
  edadMin: number;
  edadMax: number;
  trigliceridosMgDlMin: number;
  trigliceridosMgDlMax: number;
  puntosRiesgo: number;
  clasificacion: ClasificacionTrigliceridos;
  estado: EstadoRegla;
  fechaBaja: string | null;
}

export interface ReglaTrigliceridosRequest {
  sexo: SexoBiologico;
  edadMin: number;
  edadMax: number;
  trigliceridosMgDlMin: number;
  trigliceridosMgDlMax: number;
  puntosRiesgo: number;
  clasificacion: ClasificacionTrigliceridos;
}

export interface ReglaTrigliceridosFiltros {
  estado?: EstadoRegla;
  sexo?: SexoBiologico;
}
