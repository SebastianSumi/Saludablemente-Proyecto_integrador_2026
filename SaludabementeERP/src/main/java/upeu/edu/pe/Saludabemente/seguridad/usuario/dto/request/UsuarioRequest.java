package upeu.edu.pe.Saludabemente.seguridad.usuario.dto.request;

import jakarta.validation.constraints.*;

import java.util.Set;

/**
 * Payload para dar de alta una cuenta de usuario.
 *
 * <p>El {@code idPersona} se valida contra el módulo Personal (capa
 * anticorrupción) antes de persistir. La contraseña se cifra con BCrypt
 * en el servicio — nunca se guarda en texto plano.</p>
 */
public record UsuarioRequest(

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 120, message = "El email no puede superar 120 caracteres")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 80, message = "La contraseña debe tener entre 8 y 80 caracteres")
        String password,

        @NotNull(message = "El id de persona es obligatorio")
        @Positive(message = "El id de persona debe ser positivo")
        Long idPersona,

        @NotEmpty(message = "Debe asignar al menos un rol")
        Set<@Positive Long> rolesIds
) {}