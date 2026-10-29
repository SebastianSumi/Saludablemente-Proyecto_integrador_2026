package upeu.edu.pe.Saludabemente.seguridad.api.noticias;

import java.util.List;

/**
 * Snapshot de usuario expuesto al módulo Noticias.
 *
 * <p>Contrato de solo lectura. Alineado con la nomenclatura que Noticias
 * declaró en su especificación (campo {@code id}, no {@code idUsuario}).</p>
 *
 * <p>Todos los campos provienen de la tabla {@code USUARIO} de Seguridad,
 * incluido {@code nombreCompleto}, que se arma desde el snapshot de
 * identidad capturado al dar de alta la cuenta. No hay llamada a Personal
 * para construir este DTO.</p>
 *
 * @param id             PK del usuario en Seguridad
 * @param email          correo institucional
 * @param idPersona      FK lógica a PERSONA (módulo Personal)
 * @param nombreCompleto nombres + apellido paterno + apellido materno
 * @param roles          nombres de roles (orden alfabético)
 * @param activo         estado de la cuenta en Seguridad
 */
public record UsuarioNoticiasDTO(
        Long id,
        String email,
        Long idPersona,
        String nombreCompleto,
        List<String> roles,
        Boolean activo
) {}