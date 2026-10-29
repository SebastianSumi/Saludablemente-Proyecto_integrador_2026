package upeu.edu.pe.Saludabemente.personal.persona.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CelularUpdateRequest(
        @NotBlank(message = "El celular es obligatorio")
        @Pattern(
                regexp = "^\\+?[0-9]{9,15}$",
                message = "El celular debe tener entre 9 y 15 dígitos, opcionalmente con prefijo +"
        )
        String celular
) {
}
