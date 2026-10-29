package upeu.edu.pe.Saludabemente.personal.preferencia.dto;

import java.time.LocalTime;

/**
 * DTO mínimo para el módulo Notificaciones.
 * Solo expone lo que ese módulo necesita para decidir si enviar y por dónde.
 */
public record NotificationRoutingDTO(
        Long personaId,
        String celular,
        String canalPreferido,
        LocalTime horarioContactoInicio,
        LocalTime horarioContactoFin,
        Boolean aceptaRecordatorios
) {
}