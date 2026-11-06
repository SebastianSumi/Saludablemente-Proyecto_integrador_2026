import type { EtiquetaAsignada } from '../etiquetas/etiqueta.model';
// ============================================================
// Enums del backend
// ============================================================

export type SeveridadAlerta = 'LEVE' | 'MODERADA' | 'CRITICA';

export type EstadoAlerta = 'PENDIENTE' | 'ATENDIDA' | 'ANULADA';

export type TipoEvaluacionOrigen = 'NUTRICIONAL' | 'APTITUD_FISICA';

// ============================================================
// Detalle de una alerta
// ============================================================

export interface AlertaClinicaDetalle {
  idDetalle: number;
  tipoIndicador: string;
  valorMedido: number;
  valorMin: number | null;
  valorMax: number | null;
  clasificacion: string;
  puntosRiesgo: number;
  mensaje: string;
  motorDetonante: string;
}

// ============================================================
// Alerta completa
// ============================================================

export interface AlertaClinica {
  idAlerta: number;
  personaId: number;
  evaluacionOrigenId: number;
  tipoEvaluacionOrigen: TipoEvaluacionOrigen;
  puntajeTotal: number;
  nivelSeveridad: SeveridadAlerta;
  mensajeResumen: string;
  estado: EstadoAlerta;
  fechaGeneracion: string;
  fechaAtencion: string | null;
  usuarioAtiendeId: number | null;
  observacionAtencion: string | null;
  detalles: AlertaClinicaDetalle[];
  etiquetas?: EtiquetaAsignada[];
}

// ============================================================
// Resumen (para dashboard y listados livianos)
// ============================================================

export interface AlertaClinicaResumen {
  idAlerta: number;
  personaId: number;
  tipoEvaluacionOrigen: string;
  puntajeTotal: number;
  nivelSeveridad: string;
  estado: string;
  fechaGeneracion: string;
  cantidadDetalles: number;
}

// ============================================================
// Reporte: agregados + listado resumido
// ============================================================

export interface AlertaAgregado {
  totalAlertas: number;
  totalCriticas: number;
  totalAtendidas: number;
}

export interface AlertaReporte {
  agregado: AlertaAgregado;
  alertas: AlertaClinicaResumen[];
}

// ============================================================
// Request: atender alerta
// ============================================================

export interface AtenderAlertaRequest {
  idUsuario: number;
  observacion: string;
}

// ============================================================
// Filtros de búsqueda (todos opcionales)
// ============================================================

export interface AlertaFiltros {
  etiqueta?: string;
  estado?: EstadoAlerta;
  severidad?: SeveridadAlerta;
  personaId?: number;
  tipoOrigen?: TipoEvaluacionOrigen;
  desde?: string;
  hasta?: string;
  ordenarPor?: string;
  direccion?: 'ASC' | 'DESC';
}
