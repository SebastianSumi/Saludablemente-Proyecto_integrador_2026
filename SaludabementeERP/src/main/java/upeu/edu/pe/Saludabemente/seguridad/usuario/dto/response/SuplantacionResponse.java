package upeu.edu.pe.Saludabemente.seguridad.usuario.dto.response;

import java.util.Set;

/**
 * Contexto de suplantación de identidad.
 *
 * <p><b>Sub-fase 1:</b> el endpoint aún no existe. Este DTO se define
 * ahora para que el contrato quede cerrado; se usará en Sub-fase 2 cuando
 * se implemente la suplantación exclusiva para ROOT.</p>
 *
 * <p><b>Sub-fase 3:</b> el campo {@code contexto} se reemplazará por un
 * token JWT real. Hasta entonces, contiene un valor simbólico que el
 * frontend puede ignorar.</p>
 */
public record SuplantacionResponse(
        Long idUsuario,
        String email,
        Set<String> roles,
        String nombres,
        String apellidoPaterno,
        String apellidoMaterno,
        String contexto
) {}