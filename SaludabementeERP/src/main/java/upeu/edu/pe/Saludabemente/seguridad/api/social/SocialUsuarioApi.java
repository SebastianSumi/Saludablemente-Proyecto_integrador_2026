package upeu.edu.pe.Saludabemente.seguridad.api.social;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz pública de Seguridad consumida por el módulo Social.
 *
 * <p>Solo lectura. Toda la lógica de autenticación es responsabilidad de
 * Seguridad. Social no emite ni valida tokens directamente.</p>
 */
public interface SocialUsuarioApi {

    /**
     * Resuelve la identidad del usuario autenticado en el request actual.
     *
     * @throws upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException
     *         si no hay JWT válido o el email no corresponde a un usuario activo
     */
    SocialUsuarioActualDto obtenerUsuarioActual();

    /**
     * Snapshot completo de un usuario por su ID interno.
     *
     * @throws upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException
     *         si el usuario no existe o está inactivo
     */
    SocialUsuarioSnapshotDto obtenerPorId(Long idUsuario);

    /**
     * Snapshot completo por {@code username} corto (case-insensitive).
     * Devuelve {@link Optional#empty()} si no existe.
     */
    Optional<SocialUsuarioSnapshotDto> obtenerPorUsername(String username);

    /**
     * Búsqueda parcial de usuarios activos por username, email o nombre completo.
     * El límite se capea a 20. Devuelve lista vacía si no hay coincidencias.
     */
    List<SocialUsuarioBusquedaDto> buscar(String query, int limite);

    /**
     * Indica si el usuario tiene el rol "Administrador".
     * Devuelve {@code false} si el usuario no existe.
     */
    boolean esAdministrador(Long idUsuario);

    /**
     * Batch lookup de usuarios activos por IDs.
     *
     * <p>Devuelve únicamente los usuarios existentes y activos. Los IDs
     * inexistentes o de usuarios inactivos se omiten silenciosamente.
     * No preserva el orden de entrada; el consumidor debe indexar por
     * {@code idUsuario}.</p>
     *
     * <p>Si {@code ids} es null o vacío, devuelve lista vacía.</p>
     *
     * <p>El consumidor no debe enviar más de 100 identificadores por
     * invocación. Si excede ese límite, se truncan silenciosamente a los
     * primeros 100.</p>
     *
     * @param ids identificadores de usuario
     * @return lista de snapshots de usuarios activos
     */
    List<SocialUsuarioSnapshotDto> obtenerPorIds(Collection<Long> ids);
}