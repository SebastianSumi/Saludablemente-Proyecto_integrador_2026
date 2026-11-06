export type ClasificacionGlucosa =
  | 'HIPOGLUCEMIA'
  | 'NORMAL'
  | 'PREDIABETES'
  | 'DIABETES';

export type SexoBiologico = 'M' | 'F';
export type EstadoRegla = 'ACTIVO' | 'INACTIVO';

export interface ReglaGlucosaResponse {
  idReglaGlucosa: number;
  sexo: SexoBiologico;
  edadMin: number;
  edadMax: number;
  glucosaMgDlMin: number;
  glucosaMgDlMax: number;
  puntosRiesgo: number;
  clasificacion: ClasificacionGlucosa;
  estado: EstadoRegla;
  fechaBaja: string | null;
}

export interface ReglaGlucosaRequest {
  sexo: SexoBiologico;
  edadMin: number;
  edadMax: number;
  glucosaMgDlMin: number;
  glucosaMgDlMax: number;
  puntosRiesgo: number;
  clasificacion: ClasificacionGlucosa;
}

export interface ReglaGlucosaFiltros {
  estado?: EstadoRegla;
  sexo?: SexoBiologico;
}
