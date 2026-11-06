/**
 * Modelos que reflejan el contrato del módulo Catálogo - Áreas de Trabajo.
 * Coinciden 1 a 1 con AreaResponse / AreaRequest / AreaResumen.
 */

export interface Area {
  id: number;
  nombre: string;
  descripcion?: string;
  activo: boolean;
}

export interface AreaRequest {
  nombre: string;
  descripcion?: string;
}

export interface AreaResumen {
  id: number;
  nombre: string;
}
