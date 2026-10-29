package upeu.edu.pe.Saludabemente.seguridad.usuario.dto.request;

import jakarta.validation.constraints.*;

/**
 * Payload para el autorregistro público de cuentas.
 *
 * <p>La persona se identifica con <b>documento + email</b>. El rol se fija
 * en servidor (Colaborador) y nunca se recibe del cliente.</p>
 */
public record RegistroRequest(

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 120, message = "El email no puede superar 120 caracteres")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 80, message = "La contraseña debe tener entre 8 y 80 caracteres")
        String password,

        @NotBlank(message = "El documento de identidad es obligatorio")
        @Size(max = 20, message = "El documento no puede superar 20 caracteres")
        String documentoIdentidad
) {}
