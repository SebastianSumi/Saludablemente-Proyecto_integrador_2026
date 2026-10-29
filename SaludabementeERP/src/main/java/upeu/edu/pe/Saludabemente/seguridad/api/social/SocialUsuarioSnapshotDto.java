package upeu.edu.pe.Saludabemente.seguridad.api.social;

import java.util.List;

/**
 * Snapshot completo de un usuario para enriquecer autores de posts,
 * comentarios y menciones.
 *
 * @param idUsuario     PK interno en {@code SLB_SEGURIDAD.USUARIO}
 * @param username      identificador corto para menciones (ej. {@code ana.lazaro})
 * @param email         correo institucional
 * @param nombreCompleto nombre para mostrar (nombres + apellidos)
 * @param fotoUrl       URL de la foto de perfil, puede ser {@code null}
 * @param roles         nombres de roles del usuario
 * @param rolPrincipal  rol de mayor jerarquía, {@code null} si no tiene roles
 * @param nombreEquipo  nombre del equipo del colaborador, {@code null} si no aplica
 * @param activo        estado de la cuenta
 */
public record SocialUsuarioSnapshotDto(
        Long idUsuario,
        String username,
        String email,
        String nombreCompleto,
        String fotoUrl,
        List<String> roles,
        String rolPrincipal,
        String nombreEquipo,
        Boolean activo
) {}