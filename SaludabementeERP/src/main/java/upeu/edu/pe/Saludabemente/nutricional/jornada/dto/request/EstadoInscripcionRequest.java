package upeu.edu.pe.Saludabemente.nutricional.jornada.dto.request;

import jakarta.validation.constraints.NotNull;
import upeu.edu.pe.Saludabemente.nutricional.jornada.enums.EstadoInscripcion;

public record EstadoInscripcionRequest(
        @NotNull(message = "El estado de la inscripción es obligatorio")
        EstadoInscripcion estado
) {
}
