package upeu.edu.pe.Saludabemente.seguridad.usuario.dto.response;

/**
 * Respuesta del paso 1 del autorregistro.
 *
 * <p>Solo expone el nombre para la confirmación “¿eres tú?”.
 * Nunca expone {@code idPersona} al cliente anónimo.</p>
 */
public record VerificarRegistroResponse(
        String nombreMostrado
) {}
