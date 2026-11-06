/**
 * Payload de registro/edición manual de bioquímica.
 * Todos los campos son nullable — el laboratorio puede entregar solo
 * algunos marcadores (ver DECISIONES.md, A-04).
 */
export interface BioquimicoManualRequest {
  glucosa: number | null;
  colesterol: number | null;
  trigliceridos: number | null;
  presionSistolica: number | null;
  presionDiastolica: number | null;
}

export interface BioquimicoResponse {
  idBioquimico: number;
  glucosa: number | null;
  colesterol: number | null;
  trigliceridos: number | null;
  presionSistolica: number | null;
  presionDiastolica: number | null;
  dxBioquimico: string | null;
  archivoOrigen: string | null;
}

/**
 * Fila del Excel que no pudo procesarse, con motivo del rechazo.
 */
export interface FilaRechazada {
  numeroFila: number;
  celular: string;
  motivo: string;
}

/**
 * Reporte devuelto tras una importación masiva de Excel bioquímico.
 */
export interface ImportacionReporte {
  archivoOrigen: string;
  periodo: string;
  totalFilas: number;
  procesadas: number;
  rechazadas: number;
  detalleRechazos: FilaRechazada[];
}
