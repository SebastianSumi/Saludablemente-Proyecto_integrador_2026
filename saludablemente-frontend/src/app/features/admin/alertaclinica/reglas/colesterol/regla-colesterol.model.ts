export type ClasificacionColesterol = 'DESEABLE' | 'LIMITE' | 'ALTO';

export type SexoBiologico = 'M' | 'F';
export type EstadoRegla = 'ACTIVO' | 'INACTIVO';

export interface ReglaColesterolResponse {
  idReglaColesterol: number;
  sexo: SexoBiologico;
  edadMin: number;
  edadMax: number;
  colesterolMgDlMin: number;
  colesterolMgDlMax: number;
  puntosRiesgo: number;
  clasificacion: ClasificacionColesterol;
  estado: EstadoRegla;
  fechaBaja: string | null;
}

export interface ReglaColesterolRequest {
  sexo: SexoBiologico;
  edadMin: number;
  edadMax: number;
  colesterolMgDlMin: number;
  colesterolMgDlMax: number;
  puntosRiesgo: number;
  clasificacion: ClasificacionColesterol;
}

export interface ReglaColesterolFiltros {
  estado?: EstadoRegla;
  sexo?: SexoBiologico;
}
