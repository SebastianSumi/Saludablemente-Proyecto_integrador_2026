/**
 * Modelos que reflejan el contrato del módulo Teams del backend.
 * Coinciden 1 a 1 con TeamResponse / TeamRequest (`/api/v1/equipos`).
 */

export interface Team {
  id: number;
  nombre: string;
  descripcion?: string;
  activo: boolean;
  imagenUrl?: string;
  color?: string;
  slogan?: string;
  referenciaBiblica?: string;
  textoVersiculo?: string;
  idUsuarioLider?: number;
}

export interface TeamRequest {
  nombre: string;
  descripcion?: string;
  color?: string;
  slogan?: string;
  referenciaBiblica?: string;
  textoVersiculo?: string;
}
