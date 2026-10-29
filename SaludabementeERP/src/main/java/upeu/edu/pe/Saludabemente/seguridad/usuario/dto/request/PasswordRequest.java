package upeu.edu.pe.Saludabemente.seguridad.usuario.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload para cambiar la contraseña de un usuario.
 * El nuevo valor se cifra con BCrypt antes de persistir.
 */
public record PasswordRequest(

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, max = 80, message = "La contraseña debe tener entre 8 y 80 caracteres")
        String newPassword
) {}