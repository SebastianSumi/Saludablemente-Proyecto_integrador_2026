export interface Fuerza {
  repeticionesAbdominales: number;
  repeticionesFlexionBrazo: number;
  saltoLongitudCm: number;
  dxCore?: string;
  dxTrenSuperior?: string;
  dxTrenInferior?: string;
  dxFuerzaGlobal?: string;
}

export interface FuerzaRequest {
  repeticionesAbdominales: number;
  repeticionesFlexionBrazo: number;
  saltoLongitudCm: number;
}
