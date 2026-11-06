// Modelos del módulo Seguridad/Usuarios. Réplica fiel de los DTOs del
// backend (SaludabementeERP :: seguridad). Solo lectura: no tocar backend.

export interface Rol {
  id: number;
  nombre: string;
  descripcion?: string;
}

export interface Usuario {
  id: number;
  email: string;
  activo: boolean;
  fechaCreacion?: string;
  idPersona: number;
  documentoIdentidad?: string;
  nombres?: string;
  apellidoPaterno?: string;
  apellidoMaterno?: string;
  areaTrabajo?: string;
  activoPersona?: boolean;
  snapshotFecha?: string;
  roles: Rol[];
}

export interface UsuarioRequest {
  email: string;
  password: string;
  idPersona: number;
  rolesIds: number[];
}

export interface RolesRequest {
  rolesIds: number[];
}

export interface PasswordRequest {
  newPassword: string;
}

export interface Pagina<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface UsuarioFiltros {
  email?: string;
  activo?: boolean;
  page?: number;
  size?: number;
}
