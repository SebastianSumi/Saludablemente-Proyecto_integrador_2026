// Modelos del panel Mis notificaciones. Réplica fiel de los DTOs del
// backend (SaludabementeERP :: notificaciones). Solo lectura: no tocar backend.

export interface BandejaNotificacion {
  idNotificacion: number;
  titulo: string;
  mensaje: string;
  origenModulo?: string;
  idOrigenReferencia?: number;
  canal?: string;
  leido: boolean;
  fechaEnvio?: string;
}
