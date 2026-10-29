package upeu.edu.pe.Saludabemente.seguridad.api.metas;

import java.util.List;

/**
 * Snapshot del usuario que ejecuta la petición HTTP actual.
 *
 * <p>Contrato de solo lectura. No incluye {@code password_hash} ni
 * datos sensibles. El campo {@code roles} usa el vocabulario cerrado
 * cargado por {@code RolSeeder}: Administrador, Coordinador, Evaluador,
 * Colaborador.</p>
 *
 * @param idUsuario PK del usuario en Seguridad
 * @param idPersona FK lógica a PERSONA (módulo Personal)
 * @param email     email institucional único (columna {@code slb_seguridad.usuario.email})
 * @param nombres   snapshot de nombres capturado al dar de alta la cuenta
 * @param roles     códigos de rol (nunca vacío — regla del módulo)
 */
public record UsuarioActualDTO(
        Long idUsuario,
        Long idPersona,
        String email,
        String nombres,
        List<String> roles
) {}