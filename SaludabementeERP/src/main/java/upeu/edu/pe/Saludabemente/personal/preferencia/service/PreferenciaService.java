package upeu.edu.pe.Saludabemente.personal.preferencia.service;

import upeu.edu.pe.Saludabemente.personal.preferencia.dto.NotificationRoutingDTO;
import upeu.edu.pe.Saludabemente.personal.preferencia.dto.request.PreferenciaRequest;
import upeu.edu.pe.Saludabemente.personal.preferencia.dto.response.PreferenciaResponse;
import upeu.edu.pe.Saludabemente.personal.preferencia.entity.PreferenciaComunicacion;

import java.util.Optional;

public interface PreferenciaService {

    /**
     * Crea o actualiza la preferencia de una persona.
     * Si no existe, la crea con los datos del request.
     * Si ya existe, la actualiza (solo los campos enviados no nulos).
     */
    PreferenciaResponse upsertPreferencias(Long idPersona, PreferenciaRequest request);

    /**
     * Servicio PÚBLICO para el módulo Notificaciones.
     * Devuelve el celular, canal y franja horaria para enrutar un mensaje.
     * Devuelve Optional.empty() si no hay preferencia configurada.
     */
    Optional<NotificationRoutingDTO> getNotificationRoutingInfo(Long idPersona);

    /**
     * Servicio interno para otros componentes de Personal.
     * PersonaServiceImpl lo usa al construir respuestas si necesita la preferencia.
     */
    PreferenciaComunicacion buscarOFallar(Long idPersona);

    PreferenciaResponse obtenerPorPersona(Long idPersona);
}