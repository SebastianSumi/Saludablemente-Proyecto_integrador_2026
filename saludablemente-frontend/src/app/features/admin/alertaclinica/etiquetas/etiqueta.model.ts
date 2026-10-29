import { EstadoAlerta, SeveridadAlerta } from '../alerta/alerta-clinica.model';

export interface Pagina<T> { content: T[]; totalElements: number; totalPages: number; number: number; }
export interface ParametroClinico { id: number; codigo: string; nombre: string; unidad: string; }
export interface ParametroAsignado { idParametro: number; codigo: string; nombre: string; unidad: string; }
export type { EtiquetaClinicaAsignada as EtiquetaAsignada } from '../../../../shared/models/etiqueta-clinica-asignada';
export interface AlertaAsignada {
  idAlerta: number; personaId: number; nivelSeveridad: SeveridadAlerta; estado: EstadoAlerta;
  fechaGeneracion: string; asignadaPor: 'MOTOR' | 'MANUAL'; fechaAsignacion: string;
}
export interface EtiquetaResumen {
  id: number; codigo: string; nombre: string; colorHex: string; estado: 'ACTIVO' | 'INACTIVO';
  cantidadParametros: number; cantidadAlertas: number;
}
export interface EtiquetaClinica extends Omit<EtiquetaResumen, 'cantidadParametros' | 'cantidadAlertas'> {
  descripcion: string | null; fechaBaja: string | null; revision: string;
  parametros: ParametroAsignado[]; alertas: AlertaAsignada[];
}
export interface EtiquetaRequest {
  codigo: string; nombre: string; descripcion: string; colorHex: string;
  detalles: { idParametro: number }[]; revision?: string;
}
export interface TarjetaResponse<T> { revision: string; detalles: T[]; }
export type TipoAsociacion = 'alerta-etiquetas' | 'etiqueta-alertas' | 'etiqueta-parametros' | 'parametro-etiquetas';
export interface FilaAsociacion {
  id: number; nombre: string; codigo: string; descripcion?: string; color?: string;
  origen?: 'MOTOR' | 'MANUAL'; fecha?: string; ruta?: string;
}
export interface TarjetaAsociaciones { revision: string; detalles: FilaAsociacion[]; }
export interface ReporteEtiquetaFila {
  idEtiqueta: number; codigo: string; nombre: string; colorHex: string; estado: string;
  parametros: ParametroAsignado[]; cantidadAlertas: number; ultimaAsignacion: string | null; alertas: AlertaAsignada[];
}
export interface ReporteEtiquetas {
  cantidadEtiquetas: number; etiquetasActivas: number; cantidadAsociaciones: number;
  alertasUnicas: number; alertasCriticasUnicas: number; detalles: ReporteEtiquetaFila[];
}
