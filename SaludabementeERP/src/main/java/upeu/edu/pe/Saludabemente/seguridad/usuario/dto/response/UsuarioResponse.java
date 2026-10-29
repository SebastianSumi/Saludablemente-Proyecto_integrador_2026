package upeu.edu.pe.Saludabemente.seguridad.usuario.dto.response;

import upeu.edu.pe.Saludabemente.seguridad.rol.dto.response.RolResponse;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Perfil 360° de un usuario.
 *
 * <p>Fusiona tres fuentes en un solo JSON:</p>
 * <ul>
 *   <li>Credenciales y estado de la cuenta (módulo Seguridad).</li>
 *   <li>Roles asignados (módulo Seguridad).</li>
 *   <li>Snapshot de identidad capturado desde Personal al momento del alta
 *       (capa anticorrupción). No es dato vivo: es histórico.</li>
 * </ul>
 */
public record UsuarioResponse(
        Long id,
        String email,
        String username,              // ← NUEVO
        Boolean activo,
        LocalDateTime fechaCreacion,

        // Snapshot de identidad (inmutable post-alta)
        Long idPersona,
        String documentoIdentidad,
        String nombres,
        String apellidoPaterno,
        String apellidoMaterno,
        String areaTrabajo,
        String equipo,                // ← NUEVO
        Boolean activoPersona,
        LocalDateTime snapshotFecha,

        // Roles
        Set<RolResponse> roles
) {}