package upeu.edu.pe.Saludabemente.seguridad.usuario.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.Set;

/**
 * Payload para reemplazar los roles de un usuario existente.
 * Sobrescribe por completo la tabla puente USUARIO_ROL.
 */
public record RolesRequest(

        @NotEmpty(message = "Debe indicar al menos un rol")
        Set<@Positive Long> rolesIds
) {}