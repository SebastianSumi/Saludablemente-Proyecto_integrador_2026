package upeu.edu.pe.Saludabemente.personal.persona.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.personal.persona.dto.PersonalAgregado;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Persona;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Sexo;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PersonaRepository extends JpaRepository<Persona, Long> {

    @Override
    @EntityGraph(attributePaths = {"equipo", "areaTrabajo"})
    Optional<Persona> findById(Long id);

    @EntityGraph(attributePaths = {"equipo", "areaTrabajo"})
    @Query("""
        SELECT p FROM Persona p
        WHERE (:estado IS NULL OR p.activo = :estado)
          AND (:sexo IS NULL OR p.sexo = :sexo)
          AND (:areaTrabajoId IS NULL OR p.areaTrabajo.id = :areaTrabajoId)
          AND (:equipoId IS NULL OR p.equipo.id = :equipoId)
          AND (CAST(:texto AS string) IS NULL
               OR LOWER(p.nombres) LIKE LOWER(CONCAT('%', CAST(:texto AS string), '%'))
               OR LOWER(p.apellidoPaterno) LIKE LOWER(CONCAT('%', CAST(:texto AS string), '%'))
               OR LOWER(COALESCE(p.apellidoMaterno, '')) LIKE LOWER(CONCAT('%', CAST(:texto AS string), '%'))
               OR LOWER(p.documentoIdentidad) LIKE LOWER(CONCAT('%', CAST(:texto AS string), '%')))
        """)
    List<Persona> buscar(
            @Param("estado") Boolean estado,
            @Param("sexo") Sexo sexo,
            @Param("areaTrabajoId") Long areaTrabajoId,
            @Param("equipoId") Long equipoId,
            @Param("texto") String texto,
            Sort sort
    );

    @Query("""
        SELECT new upeu.edu.pe.Saludabemente.personal.persona.dto.PersonalAgregado(
            COUNT(p),
            COALESCE(SUM(CASE WHEN p.activo = true THEN 1 ELSE 0 END), 0),
            COALESCE(SUM(CASE WHEN p.sexo = 'M' THEN 1 ELSE 0 END), 0),
            COALESCE(SUM(CASE WHEN p.sexo = 'F' THEN 1 ELSE 0 END), 0)
        )
        FROM Persona p
        """)
    PersonalAgregado agregados();

    boolean existsByCelularAndActivoTrue(String celular);

    boolean existsByDocumentoIdentidad(String documentoIdentidad);

    boolean existsByEquipoIdAndActivoTrue(Long idEquipo);

    /**
     * Valida si hay personas activas en un area de trabajo.
     * Se usa para bloquear la inactivacion de un area con personal activo.
     */
    boolean existsByAreaTrabajoIdAndActivoTrue(Long idArea);

    @EntityGraph(attributePaths = {"equipo", "areaTrabajo"})
    List<Persona> findByEquipoId(Long idEquipo);
    /**
     * Proyeccion para resolver un lote de celulares a ids de persona
     * en una sola consulta. Uso interno entre modulos.
     */
    interface CelularIdProjection {
        String getCelular();
        Long getIdPersona();
    }

    /**
     * Resolucion batch de celulares a ids de persona.
     * Se usa en importaciones masivas para evitar N+1: una sola consulta
     * para todos los celulares del archivo.
     *
     * <p>Solo devuelve personas ACTIVAS. Las inactivas no se incluyen
     * en el mapa, y el consumidor las trata como "no encontradas".</p>
     */
    @Query("""
        SELECT p.celular AS celular, p.id AS idPersona
        FROM Persona p
        WHERE p.celular IN :celulares
          AND p.activo = true
        """)
    List<CelularIdProjection> findIdsByCelulares(@Param("celulares") Set<String> celulares);

    /**
     * Cuenta personas activas de un equipo. Se usa en la API publica de
     * Equipo para el reporte de participacion.
     */
    long countByEquipoIdAndActivoTrue(Long idEquipo);

    /**
     * Lista todas las personas activas, ordenables.
     * Uso: API publica para Asistencias (listarPersonalActivo).
     */
    @EntityGraph(attributePaths = {"equipo", "areaTrabajo"})
    List<Persona> findByActivoTrue(Sort sort);

    /**
     * Lista personas activas de un equipo especifico.
     * Uso: API publica para Asistencias (listarPorTeam).
     */
    @EntityGraph(attributePaths = {"equipo", "areaTrabajo"})
    List<Persona> findByEquipoIdAndActivoTrue(Long idEquipo);

    /**
     * IDs de todas las personas activas, ordenados ascendentemente por id.
     * Uso: API publica para Actividades (listarPersonasActivasIds).
     * No usa @EntityGraph porque solo proyecta el id, no carga la entidad.
     */
    @Query("SELECT p.id FROM Persona p WHERE p.activo = true ORDER BY p.id")
    List<Long> findIdsByActivoTrue();

    /**
     * Indica si existe una persona con el id dado y activo = true.
     * Uso: contrato Noticias.
     */
    boolean existsByIdAndActivoTrue(Long id);

    /**
     * Indica si la persona existe, esta activa y pertenece al area indicada.
     * Uso: contrato Noticias.
     */
    boolean existsByIdAndAreaTrabajoIdAndActivoTrue(Long id, Long idArea);

    /**
     * Lista personas activas de un area, ordenables.
     * Uso: contrato Noticias.
     */
    @EntityGraph(attributePaths = {"equipo", "areaTrabajo"})
    List<Persona> findByAreaTrabajoIdAndActivoTrue(Long idArea, Sort sort);

    /**
     * Cuenta personas activas de un area.
     * Uso: contrato Noticias.
     */
    long countByAreaTrabajoIdAndActivoTrue(Long idArea);


    /**
     * Indica si la persona existe, esta activa y pertenece al team indicado.
     * Uso: contrato Noticias.
     */
    boolean existsByIdAndEquipoIdAndActivoTrue(Long id, Long idTeam);

    /**
     * Lista personas activas de un team, ordenables.
     * Uso: contrato Noticias.
     */
    @EntityGraph(attributePaths = {"equipo", "areaTrabajo"})
    List<Persona> findByEquipoIdAndActivoTrue(Long idTeam, Sort sort);
}