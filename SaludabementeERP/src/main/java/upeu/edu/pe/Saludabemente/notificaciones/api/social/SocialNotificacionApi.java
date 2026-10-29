package upeu.edu.pe.Saludabemente.notificaciones.api.social;

/** Puente al despacho de notificaciones (síncrono, sin broker).
 * El productor invoca después del commit y excluye autoavisos.
 * La entrega de email no garantizada se reintenta vía DLQ en tabla.
 */
public interface SocialNotificacionApi {
    void enviar(SocialAvisoDto aviso);
}
