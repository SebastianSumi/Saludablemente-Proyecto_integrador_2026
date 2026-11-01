package upeu.edu.pe.Saludabemente.seguridad.usuario.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.seguridad.usuario.entity.Usuario;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @EntityGraph(attributePaths = {"roles"})
    List<Usuario> findByActivoTrueAndIdPersonaSnapshotIn(Collection<Long> idsPersona);

    /**
     * findById con roles precargados en un solo query.
     * Evita el problema N+1 al armar el perfil 360°.
     */
    @EntityGraph(attributePaths = {"roles"})
    Optional<Usuario> findById(Long id);

    /**
     * Validación ultrarrápida de unicidad de email.
     */
    boolean existsByEmail(String email);

    /**
     * Garante de la regla 1:1 (una persona = una cuenta).
     */
    boolean existsByIdPersonaSnapshot(Long idPersonaSnapshot);

    /**
     * Búsqueda paginada con filtros dinámicos.
     *
     * <p>El parámetro {@code excluirRoot} se usa cuando el solicitante
     * NO es ROOT: la consulta excluye cualquier cuenta que tenga el rol
     * ROOT. En Sub-fase 1 siempre se pasa {@code false} porque ROOT aún
     * no existe; en Sub-fase 2 se activa el filtro fantasma.</p>
     *
     * <p>No se precargan roles en la lista (evita N+1 con paginación).
     * El {@code UsuarioResponse} del listado expone datos resumidos;
     * los roles se cargan solo en {@code GET /usuarios/{id}}.</p>
     */
    @Query("""
        SELECT u FROM Usuario u
        WHERE (CAST(:email AS string) IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:email AS string), '%')))
          AND (:activo IS NULL OR u.activo = :activo)
          AND (:excluirRoot = false OR NOT EXISTS (
                SELECT 1 FROM u.roles r WHERE r.nombre = 'ROOT'
          ))
    """)
    Page<Usuario> buscarDinamico(
            @Param("email") String email,
            @Param("activo") Boolean activo,
            @Param("excluirRoot") boolean excluirRoot,
            Pageable pageable
    );

    /**
     * ¿El usuario indicado tiene el rol ROOT?
     * Usado por el filtro fantasma y por las guard clauses de Sub-fase 2.
     */
    @Query("""
        SELECT COUNT(u) > 0 FROM Usuario u
        JOIN u.roles r
        WHERE u.id = :idUsuario AND r.nombre = 'ROOT'
    """)
    boolean esRoot(@Param("idUsuario") Long idUsuario);

    /**
     * Lista usuarios activos que tengan el rol indicado.
     * Usado por el contrato público hacia Actividades (combo de coordinadores).
     *
     * <p>El nombre del rol se compara case-insensitive para evitar errores
     * del frontend ("coordinador" vs "Coordinador").</p>
     */
    @Query("""
    SELECT DISTINCT u FROM Usuario u
    JOIN u.roles r
    WHERE LOWER(r.nombre) = LOWER(:nombreRol)
      AND u.activo = true
""")
    List<Usuario> findActivosByRolNombre(@Param("nombreRol") String nombreRol);

    /**
     * Busca un usuario por email exacto.
     * Usado por el contrato hacia Noticias (obtenerPorEmail).
     *
     * <p>Comparación case-sensitive: los emails se normalizan a minúsculas
     * al crear la cuenta (ver UsuarioServiceImpl.crear).</p>
     */
    @EntityGraph(attributePaths = {"roles"})
    Optional<Usuario> findByEmail(String email);

    /**
     * Busca un usuario por su vínculo lógico a Persona.
     * Usado por el contrato hacia Noticias (obtenerPorIdPersona).
     */
    @EntityGraph(attributePaths = {"roles"})
    Optional<Usuario> findByIdPersonaSnapshot(Long idPersonaSnapshot);
    /**
     * Validación ultrarrápida de unicidad de username.
     * Usado por el generador de username al crear la cuenta.
     */
    boolean existsByUsername(String username);

    /**
     * Busca un usuario por username exacto.
     * Usado por el contrato hacia Social.
     */
    @EntityGraph(attributePaths = {"roles"})
    Optional<Usuario> findByUsername(String username);

    /**
     * Búsqueda de usuarios activos para autocompletado de menciones.
     *
     * <p>Busca coincidencia parcial (case-insensitive) en {@code username},
     * {@code email} o nombre completo (nombres + apellidos). Solo devuelve
     * usuarios activos. Ordenado por apellido paterno y nombres.</p>
     *
     * <p>Usado por el contrato público hacia Social. El llamador limita
     * el resultado a 20 vía {@link Pageable}.</p>
     */
    @Query("""
    SELECT DISTINCT u FROM Usuario u
    WHERE u.activo = true
      AND (
        LOWER(u.username) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%'))
        OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%'))
        OR LOWER(CONCAT(u.nombresSnapshot, ' ', u.apellidoPaternoSnapshot,
                  ' ', COALESCE(u.apellidoMaternoSnapshot, '')))
             LIKE LOWER(CONCAT('%', CAST(:q AS string), '%'))
      )
    ORDER BY u.apellidoPaternoSnapshot, u.nombresSnapshot
""")
    List<Usuario> buscarParaMenciones(@Param("q") String query, Pageable pageable);

    /**
     * Batch lookup de usuarios activos por IDs.
     * Usado por el contrato hacia Social (obtenerPorIds) para resolver
     * autores de una página de feed en una sola consulta.
     *
     * <p>Filtra por {@code activo = true}. Los IDs inactivos o inexistentes
     * se omiten automáticamente.</p>
     */
    @EntityGraph(attributePaths = {"roles"})
    @Query("""
    SELECT u FROM Usuario u
    WHERE u.id IN :ids AND u.activo = true
""")
    List<Usuario> findActivosByIdIn(@Param("ids") Collection<Long> ids);
}
