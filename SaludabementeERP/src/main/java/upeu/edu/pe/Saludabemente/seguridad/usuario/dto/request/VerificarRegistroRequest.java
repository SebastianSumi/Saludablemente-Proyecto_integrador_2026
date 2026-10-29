package upeu.edu.pe.Saludabemente.seguridad.usuario.dto.request;

import jakarta.validation.constraints.*;

/**
 * Payload del paso 1 del autorregistro: verifica que la persona exista,
 * esté activa y no tenga cuenta, antes de pedir la contraseña.
 */
public record VerificarRegistroRequest(

        @NotBlank(message = "El documento de identidad es obligatorio")
        @Size(max = 20, message = "El documento no puede superar 20 caracteres")
        String documentoIdentidad,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 120, message = "El email no puede superar 120 caracteres")
        String email
) {}
