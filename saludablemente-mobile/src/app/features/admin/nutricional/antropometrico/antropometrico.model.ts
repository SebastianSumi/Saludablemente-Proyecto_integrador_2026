export interface AntropometricoRequest {
  estaturaCm: number;
  pesoKg: number;
  perimetroAbdominalCm?: number;
  porcentajeGrasa?: number;
  porcentajeMasaMuscular?: number;
  porcentajeGrasaVisceral?: number;
}

export interface AntropometricoResponse {
  idAntropometrico: number;
  estaturaCm: number;
  pesoKg: number;
  perimetroAbdominalCm?: number;
  imc?: number;
  porcentajeGrasa?: number;
  porcentajeMasaMuscular?: number;
  porcentajeGrasaVisceral?: number;
  dxImc?: string;
  dxPerimetroAbdominal?: string;
  dxGrasa?: string;
  dxMasaMuscular?: string;
  dxGrasaVisceral?: string;
}
