package upeu.edu.pe.Saludabemente.seguridad.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "El email o usuario es obligatorio")
        @Size(max = 120)
        String identificador,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(max = 80)
        String password
) {
}
