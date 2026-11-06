// Réplica fiel de los DTOs del backend (solo lectura del backend, sin tocarlo).
export type CanalNotificacion = 'EMAIL' | 'IN_APP' | 'PUSH' | 'SMS';

export interface Notificacion {
  idNotificacion: number;
  idPersona: number;
  canal: string;
  titulo: string;
  mensaje: string;
  origenModulo?: string;
  idOrigenReferencia?: number;
  estadoLectura?: string;
  estadoDelivery?: string;
  fechaEnvio?: string;
}

export interface BandejaNotificacion {
  idNotificacion: number;
  titulo: string;
  mensaje: string;
  origenModulo?: string;
  idOrigenReferencia?: number;
  leido?: boolean;
  fechaEnvio?: string;
}

export interface Plantilla {
  idPlantilla: number;
  codigoEvento: string;
  canal: string;
  asunto?: string;
  cuerpoHtml?: string;
  activo?: boolean;
}

export interface PlantillaRequest {
  codigoEvento: string;
  canal: CanalNotificacion;
  asunto?: string;
  cuerpoHtml: string;
  activo?: boolean;
}
