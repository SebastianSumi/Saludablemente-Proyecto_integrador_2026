export interface UsuarioPerfil {
  idUsuario: number;
  idPersona: number;
  email: string;
  nombreCompleto: string;
  documentoIdentidad: string;
  areaTrabajo?: string;
  activo: boolean;
  fechaCreacion: string;
  roles: string[];
}
