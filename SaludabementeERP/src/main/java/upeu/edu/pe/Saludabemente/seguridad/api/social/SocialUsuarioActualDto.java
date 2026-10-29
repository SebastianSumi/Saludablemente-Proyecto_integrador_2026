package upeu.edu.pe.Saludabemente.seguridad.api.social;

import java.util.List;

/**
 * Identidad del usuario autenticado en el request actual, resuelta desde
 * el JWT emitido por el backend.
 *
 * @param idUsuario      PK interno en Seguridad
 * @param username       identificador corto
 * @param email          correo institucional
 * @param roles          nombres de roles del usuario
 * @param esAdministrador {@code true} si {@code roles} contiene "Administrador"
 */
public record SocialUsuarioActualDto(
        Long idUsuario,
        String username,
        String email,
        List<String> roles,
        boolean esAdministrador
) {}