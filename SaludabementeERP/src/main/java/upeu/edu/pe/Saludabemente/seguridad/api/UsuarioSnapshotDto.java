package upeu.edu.pe.Saludabemente.seguridad.api;

import java.util.List;

/**
 * Snapshot de usuario expuesto a otros módulos.
 *
 * <p>Representación de <b>solo lectura</b>: no incluye contraseñas ni
 * detalles internos. Es el único DTO que los módulos consumidores
 * (Actividades, Teams) pueden recibir desde Seguridad.</p>
 *
 * <p><b>Origen de los datos:</b> todos los campos provienen de la propia
 * tabla {@code USUARIO} (incluido {@code nombreCompleto}, que se arma
 * desde el snapshot de identidad capturado al dar de alta la cuenta).
 * <b>No hay llamada al módulo Personal</b> para construir este DTO.</p>
 *
 * @param idUsuario      PK del usuario en Seguridad
 * @param idPersona      FK lógica a PERSONA (módulo Personal)
 * @param nombreCompleto nombres + apellido paterno + apellido materno
 * @param email          correo institucional único
 * @param roles          nombres de roles (ej. ["Coordinador", "Evaluador"])
 * @param activo         estado de la cuenta en Seguridad (no de la persona en RRHH)
 */
public record UsuarioSnapshotDto(
        Long idUsuario,
        Long idPersona,
        String nombreCompleto,
        String email,
        List<String> roles,
        Boolean activo
) {}