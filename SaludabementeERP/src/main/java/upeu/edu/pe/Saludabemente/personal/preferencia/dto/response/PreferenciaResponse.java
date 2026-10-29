package upeu.edu.pe.Saludabemente.personal.preferencia.dto.response;

import java.time.LocalTime;

public record PreferenciaResponse(
        Long id,
        Long personaId,
        String canalPreferido,
        LocalTime horarioContactoInicio,
        LocalTime horarioContactoFin,
        Boolean aceptaRecordatorios
) {}
