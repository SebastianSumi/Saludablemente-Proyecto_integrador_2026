package upeu.edu.pe.Saludabemente.seguridad.api;

import java.util.List;

/**
 * Contrato público del módulo Seguridad hacia otros módulos.
 *
 * <p><b>Solo lectura.</b> Las operaciones de escritura (crear usuario,
 * cambiar roles, inactivar) viven en {@code UsuarioService}, que es
 * interno y <b>no</b> se expone a otros módulos.</p>
 *
 * <p><b>Sin JWT todavía:</b> la comunicación es por inyección de
 * dependencias (mismo JAR, monolito modular). Cuando llegue la fase JWT,
 * este contrato no cambia: solo se reemplaza la identificación del
 * solicitante internamente.</p>
 */
public interface SeguridadService {

    /**
     * Valida que un usuario sea apto para coordinador de actividades.
     *
     * <p>Verifica tres condiciones en orden:</p>
     * <ol>
     *   <li>El usuario existe.</li>
     *   <li>La cuenta está activa.</li>
     *   <li>Tiene rol {@code Coordinador} o {@code Evaluador}.</li>
     * </ol>
     *
     * @param idUsuario PK del usuario a validar
     * @throws upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException
     *         si el usuario no existe
     * @throws upeu.edu.pe.Saludabemente.exception.BusinessRuleException
     *         si la cuenta está inactiva o el usuario no tiene el rol requerido
     */
    void validarCoordinador(Long idUsuario);

    /**
     * Devuelve el snapshot de un usuario existente.
     *
     * @param idUsuario PK del usuario
     * @throws upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException
     *         si el usuario no existe
     */
    UsuarioSnapshotDto obtenerUsuarioPorId(Long idUsuario);

    /**
     * Lista usuarios activos que tengan el rol indicado.
     *
     * <p>La comparación del nombre del rol es case-insensitive: {@code "coordinador"}
     * y {@code "Coordinador"} devuelven el mismo resultado. Si el rol no
     * existe en el catálogo o no hay usuarios con ese rol, devuelve lista vacía.</p>
     *
     * @param rol nombre del rol (ej. "Coordinador", "Evaluador")
     * @return lista de snapshots, nunca {@code null}
     */
    List<UsuarioSnapshotDto> listarUsuariosPorRol(String rol);

    /**
     * Comprueba si un usuario existe y está activo.
     * No lanza excepción: devuelve {@code false} si no existe o está inactivo.
     *
     * @param idUsuario PK del usuario
     */
    boolean validarUsuarioActivo(Long idUsuario);

    // En SeguridadService.java
    UsuarioPerfilDTO obtenerMiPerfil();
}