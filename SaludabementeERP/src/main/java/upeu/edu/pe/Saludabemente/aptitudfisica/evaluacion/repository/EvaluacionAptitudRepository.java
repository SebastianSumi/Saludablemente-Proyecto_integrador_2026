package upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.dto.resumen.AptitudAgregadoDto;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.entity.EvaluacionAptitud;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.enums.EstadoEvaluacion;
import java.time.LocalDate;

import java.util.List;
import java.util.Optional;

public interface EvaluacionAptitudRepository extends JpaRepository<EvaluacionAptitud, Long> {

    /** Evita N+1: trae cabecera + fuerza + cardio en una sola query LEFT JOIN. */
    @EntityGraph(attributePaths = {"detalleFuerza", "detalleCardio"})
    @Query("SELECT e FROM EvaluacionAptitud e WHERE e.id = :id")
    Optional<EvaluacionAptitud> findByIdConDetalle(@Param("id") Long id);

    boolean existsByIdPersonaAndIdPeriodo(Long idPersona, Long idPeriodo);

    @EntityGraph(attributePaths = {"detalleFuerza", "detalleCardio"})
    List<EvaluacionAptitud> findByIdPersonaOrderByFechaEvaluacionDesc(Long idPersona);

    /** Búsqueda dinámica: todos los filtros son opcionales. */
    @Query("""
<<<<<<< HEAD
            SELECT e FROM EvaluacionAptitud e
            WHERE (:idPeriodo IS NULL OR e.idPeriodo = :idPeriodo)
              AND (:estado    IS NULL OR e.estadoEvaluacion = :estado)
              AND (CAST(:area AS string) IS NULL OR LOWER(e.areaTrabajoAlMomento) LIKE LOWER(CONCAT('%', CAST(:area AS string), '%')))
              AND (CAST(:texto AS string) IS NULL OR LOWER(e.nombresCompletosAlMomento) LIKE LOWER(CONCAT('%', CAST(:texto AS string), '%'))
                                      OR LOWER(e.documentoIdentidadAlMomento) LIKE LOWER(CONCAT('%', CAST(:texto AS string), '%')))
            """)
=======
        SELECT e FROM EvaluacionAptitud e
        WHERE (:idPeriodo IS NULL OR e.idPeriodo = :idPeriodo)
          AND (:estado IS NULL OR e.estadoEvaluacion = :estado)
          AND (
              COALESCE(:area, '') = ''
              OR LOWER(e.areaTrabajoAlMomento)
                 LIKE CONCAT('%', LOWER(CAST(:area AS string)), '%')
          )
          AND (
              COALESCE(:texto, '') = ''
              OR LOWER(e.nombresCompletosAlMomento)
                 LIKE CONCAT('%', LOWER(CAST(:texto AS string)), '%')
              OR LOWER(e.documentoIdentidadAlMomento)
                 LIKE CONCAT('%', LOWER(CAST(:texto AS string)), '%')
          )
        """)
>>>>>>> 75a07c82be6a122f8c84b1de8df4185dadd4faef
    Page<EvaluacionAptitud> buscar(
            @Param("idPeriodo") Long idPeriodo,
            @Param("estado") EstadoEvaluacion estado,
            @Param("area") String area,
            @Param("texto") String texto,
            Pageable pageable
    );

    /**
     * Métricas agregadas por período para el dashboard.
     * Una sola query — evita 6 COUNT separados.
     */
    @Query("""
            SELECT new upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.dto.resumen.AptitudAgregadoDto(
                COUNT(e),
                COUNT(CASE WHEN e.estadoEvaluacion = upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.enums.EstadoEvaluacion.PENDIENTE THEN 1 END),
                COUNT(CASE WHEN e.estadoEvaluacion = upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.enums.EstadoEvaluacion.COMPLETA  THEN 1 END),
                COUNT(CASE WHEN e.estadoEvaluacion = upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.enums.EstadoEvaluacion.ANULADA   THEN 1 END),
                COUNT(CASE WHEN e.dictamenGlobal = upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.enums.DictamenGlobal.APTO THEN 1 END),
                COUNT(CASE WHEN e.dictamenGlobal = upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.enums.DictamenGlobal.APTO_CON_RESTRICCIONES THEN 1 END),
                COUNT(CASE WHEN e.dictamenGlobal = upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.enums.DictamenGlobal.NO_APTO THEN 1 END)
            )
            FROM EvaluacionAptitud e
            WHERE e.idPeriodo = :idPeriodo
            """)
    AptitudAgregadoDto obtenerAgregados(@Param("idPeriodo") Long idPeriodo);

    /**
     * Consulta para Recomendaciones IA: evaluación de una persona en una
     * fecha exacta. Trae detalleFuerza y detalleCardio en una sola query
     * (evita N+1). Usado por {@code obtenerPorPersonaYFecha}.
     */
    @EntityGraph(attributePaths = {"detalleFuerza", "detalleCardio"})
    Optional<EvaluacionAptitud> findFirstByIdPersonaAndFechaEvaluacionOrderByIdDesc(
            Long idPersona, LocalDate fechaEvaluacion);

    /**
     * Consulta para Recomendaciones IA: ¿existe evaluación en un estado
     * concreto para esa persona en ese período? Usado por
     * {@code existePruebaFisica} (que pasa {@code EstadoEvaluacion.COMPLETA}).
     */
    boolean existsByIdPersonaAndIdPeriodoAndEstadoEvaluacion(
            Long idPersona, Long idPeriodo, EstadoEvaluacion estado);
}
