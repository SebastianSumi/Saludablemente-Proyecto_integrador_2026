/** Fotocopia historica compartida por las vistas de consulta. */
export interface EtiquetaClinicaAsignada {
  idEtiqueta: number;
  codigo: string;
  nombre: string;
  colorHex: string;
  asignadaPor: 'MOTOR' | 'MANUAL';
  fechaAsignacion: string;
}
