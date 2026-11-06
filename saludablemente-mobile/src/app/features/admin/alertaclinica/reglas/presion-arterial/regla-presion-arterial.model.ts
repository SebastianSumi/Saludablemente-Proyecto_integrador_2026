export type ClasificacionPresionArterial =
  | 'HIPOTENSION'
  | 'NORMAL'
  | 'ELEVADA'
  | 'HIPERTENSION_1'
  | 'HIPERTENSION_2'
  | 'CRISIS_HIPERTENSIVA';

export type SexoBiologico = 'M' | 'F';
export type EstadoRegla = 'ACTIVO' | 'INACTIVO';

export interface ReglaPresionArterialResponse {
  idReglaPresion: number;
  sexo: SexoBiologico;
  edadMin: number;
  edadMax: number;
  sistolicaMmHgMin: number;
  sistolicaMmHgMax: number;
  diastolicaMmHgMin: number;
  diastolicaMmHgMax: number;
  puntosRiesgo: number;
  clasificacion: ClasificacionPresionArterial;
  estado: EstadoRegla;
  fechaBaja: string | null;
}

export interface ReglaPresionArterialRequest {
  sexo: SexoBiologico;
  edadMin: number;
  edadMax: number;
  sistolicaMmHgMin: number;
  sistolicaMmHgMax: number;
  diastolicaMmHgMin: number;
  diastolicaMmHgMax: number;
  puntosRiesgo: number;
  clasificacion: ClasificacionPresionArterial;
}

export interface ReglaPresionArterialFiltros {
  estado?: EstadoRegla;
  sexo?: SexoBiologico;
}
