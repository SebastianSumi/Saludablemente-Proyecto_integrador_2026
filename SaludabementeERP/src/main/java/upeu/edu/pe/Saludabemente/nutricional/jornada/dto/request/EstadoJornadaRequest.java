package upeu.edu.pe.Saludabemente.nutricional.jornada.dto.request;

import jakarta.validation.constraints.NotNull;
import upeu.edu.pe.Saludabemente.nutricional.jornada.enums.EstadoJornada;

public record EstadoJornadaRequest(
        @NotNull(message = "El estado es obligatorio")
        EstadoJornada estado
) {
}
