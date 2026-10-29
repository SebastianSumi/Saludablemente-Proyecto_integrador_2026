package upeu.edu.pe.Saludabemente.nutricional.evaluacion.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.entity.EvaluacionNutricional;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.enums.EstadoEvaluacion;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EvaluacionRepository extends JpaRepository<EvaluacionNutricional, Long> {

    // ===================================================================
    // Consultas puntuales
    // ===================================================================

    @Override
    @EntityGraph(attributePaths = {"detalleAntropometrico", "detalleBioquimico"})
    Optional<EvaluacionNutricional> findById(Long id);

    // ===================================================================
    // Búsqueda paginada con filtros tolerantes a nulos
    // ===================================================================

    @EntityGraph(attributePaths = {"detalleAntropometrico"})
    @Query("""
            SELECT e FROM EvaluacionNutricional e
            WHERE (:estado IS NULL OR e.estadoEvaluacion = :estado)
              AND (:periodo IS NULL OR e.idPeriodo = :periodo)
              AND (:idPersona IS NULL OR e.idPersona = :idPersona)
            """)
    Page<EvaluacionNutricional> buscar(
            @Param("estado") EstadoEvaluacion estado,
            @Param("periodo") String periodo,
            @Param("idPersona") Long idPersona,
            Pageable pageable);

    // ===================================================================
    // Historial por persona
    // ===================================================================

    @EntityGraph(attributePaths = {"detalleAntropometrico", "detalleBioquimico"})
    List<EvaluacionNutricional> findByIdPersonaOrderByFechaEvaluacionDesc(Long idPersona);

    // ===================================================================
    // Unicidad semestral
    // ===================================================================

    @Query("""
            SELECT COUNT(e) > 0 FROM EvaluacionNutricional e
            WHERE e.idPersona = :idPersona
              AND e.idPeriodo = :idPeriodo
              AND e.estadoEvaluacion IN :estados
            """)
    boolean existsActivaPorPersonaYPeriodo(
            @Param("idPersona") Long idPersona,
            @Param("idPeriodo") String idPeriodo,
            @Param("estados") Collection<EstadoEvaluacion> estados);

    // ===================================================================
    // Evaluaciones incompletas
    // ===================================================================

    @EntityGraph(attributePaths = {"detalleAntropometrico", "detalleBioquimico"})
    @Query("""
            SELECT e FROM EvaluacionNutricional e
            WHERE e.idPeriodo = :periodo
              AND e.estadoEvaluacion = :estado
            """)
    List<EvaluacionNutricional> findIncompletasByPeriodo(
            @Param("periodo") String periodo,
            @Param("estado") EstadoEvaluacion estado);

    // ===================================================================
    // Conteos y mutaciones
    // ===================================================================

    long countByEstadoEvaluacion(EstadoEvaluacion estado);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE EvaluacionNutricional e
               SET e.estadoEvaluacion = :estado
             WHERE e.idEvaluacion = :id
            """)
    int softDelete(@Param("id") Long id,
                   @Param("estado") EstadoEvaluacion estado);

    // ===================================================================
    // Reportes
    // ===================================================================

    @EntityGraph(attributePaths = {"detalleBioquimico"})
    @Query("""
            SELECT e FROM EvaluacionNutricional e
             JOIN e.detalleBioquimico b
            WHERE e.idPeriodo = :periodo
              AND b.dxBioquimico LIKE 'ALTERADO%'
            """)
    List<EvaluacionNutricional> findBioquimicosAlteradosByPeriodo(
            @Param("periodo") String periodo);

    @Query("""
        SELECT e.idEvaluacion, e.idPersona, e.documentoIdentidad, e.nombresCompletos,
               e.areaTrabajoAlMomento, e.idPeriodo, e.fechaEvaluacion,
               e.estadoEvaluacion, a.imc, a.dxImc, b.dxBioquimico
          FROM EvaluacionNutricional e
          LEFT JOIN e.detalleAntropometrico a
          LEFT JOIN e.detalleBioquimico b
         WHERE e.idPeriodo = :periodo
        """)
    List<Object[]> findResumenConsolidadoByPeriodo(@Param("periodo") String periodo);

    // ===================================================================
    // Agregados para dashboard
    // ===================================================================

    @Query("""
        SELECT COUNT(e),
               AVG(a.imc),
               AVG(a.porcentajeGrasa),
               AVG(a.porcentajeMasaMuscular)
          FROM EvaluacionNutricional e
          LEFT JOIN e.detalleAntropometrico a
         WHERE (:periodo IS NULL OR e.idPeriodo = :periodo)
           AND e.estadoEvaluacion <> :estadoExcluido
        """)
    List<Object[]> agregados(@Param("periodo") String periodo,
                             @Param("estadoExcluido") EstadoEvaluacion estadoExcluido);

    // ===================================================================
    // Búsqueda por persona + periodo (importación bioquímica)
    // ===================================================================

    @EntityGraph(attributePaths = {"detalleAntropometrico", "detalleBioquimico"})
    @Query("""
        SELECT e FROM EvaluacionNutricional e
        WHERE e.idPersona = :idPersona
          AND e.idPeriodo = :idPeriodo
          AND e.estadoEvaluacion = :estado
        """)
    Optional<EvaluacionNutricional> findPendientePorPersonaYPeriodo(
            @Param("idPersona") Long idPersona,
            @Param("idPeriodo") String idPeriodo,
            @Param("estado") EstadoEvaluacion estado);
}