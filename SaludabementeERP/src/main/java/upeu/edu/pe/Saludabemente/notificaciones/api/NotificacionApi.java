package upeu.edu.pe.Saludabemente.notificaciones.api;

import java.util.Map;

public interface NotificacionApi {

    /**
     * Envía una notificación In-App al destinatario y, opcionalmente,
     * un correo si se pasa emailDestino.
     *
     * @param idPersona          destinatario en el sistema
     * @param codigoEvento       código de la plantilla a usar (ej. "ALERTA_CRITICA")
     * @param origenModulo       módulo que originó la notificación (ej. "alertas_clinicas")
     * @param idOrigenReferencia id del registro del módulo origen (polimórfico)
     * @param variables          valores para los placeholders {{...}} de la plantilla
     * @param emailDestino       correo del destinatario; null o blanco si solo In-App
     */
    void enviar(Long idPersona,
                String codigoEvento,
                String origenModulo,
                Long idOrigenReferencia,
                Map<String, Object> variables,
                String emailDestino);
}