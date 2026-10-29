package upeu.edu.pe.Saludabemente.personal.preferencia.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record PreferenciaRequest(
        @NotBlank(message = "El canal preferido es obligatorio")
        @Size(max = 30, message = "El canal preferido no puede superar 30 caracteres")
        String canalPreferido,

        LocalTime horarioContactoInicio,

        LocalTime horarioContactoFin,

        Boolean aceptaRecordatorios
) {}
