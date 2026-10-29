package upeu.edu.pe.Saludabemente.alertaclinica.alerta.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto.AlertaAgregado;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto.AlertaClinicaResumen;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.AlertaClinica;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.EstadoAlerta;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.SeveridadAlerta;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.TipoEvaluacionOrigen;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AlertaClinicaRepository extends JpaRepository<AlertaClinica, Long> {

    @EntityGraph(attributePaths = {"detalles", "etiquetas"})
    Optional<AlertaClinica> findByIdAlertaAndPersonaId(Long idAlerta, Long personaId);

    @Query(value = """
        select new upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto.AlertaClinicaResumen(
            a.idAlerta, a.personaId, a.tipoEvaluacionOrigen, a.puntajeTotal,
            a.nivelSeveridad, a.estado, a.fechaGeneracion, size(a.detalles))
        from AlertaClinica a
        where (:personaId is null or a.personaId = :personaId)
          and (:estado is null or a.estado = :estado)
          and (:severidad is null or a.nivelSeveridad = :severidad)
          and (:desde is null or a.fechaGeneracion >= :desde)
          and (:hasta is null or a.fechaGeneracion <= :hasta)
        order by a.fechaGeneracion desc, a.idAlerta desc
        """, countQuery = """
        select count(a) from AlertaClinica a
        where (:personaId is null or a.personaId = :personaId)
          and (:estado is null or a.estado = :estado)
          and (:severidad is null or a.nivelSeveridad = :severidad)
          and (:desde is null or a.fechaGeneracion >= :desde)
          and (:hasta is null or a.fechaGeneracion <= :hasta)
        """)
    org.springframework.data.domain.Page<AlertaClinicaResumen> buscarOpciones(
            @Param("personaId") Long personaId, @Param("estado") EstadoAlerta estado,
            @Param("severidad") SeveridadAlerta severidad, @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta, org.springframework.data.domain.Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"detalles", "etiquetas"})
    Optional<AlertaClinica> findById(Long id);

    Optional<AlertaClinica> findByEvaluacionOrigenIdAndTipoEvaluacionOrigen(
            Long evaluacionOrigenId, TipoEvaluacionOrigen tipo);

    boolean existsByIdAlertaAndPersonaIdAndEstado(
            Long idAlerta, Long personaId, EstadoAlerta estado);

    /**
     * Busqueda sin filtro de etiquetas.
     */
    @EntityGraph(attributePaths = {"detalles", "etiquetas"})
    @Query("""
        SELECT a FROM AlertaClinica a
        WHERE (:estado IS NULL OR a.estado = :estado)
          AND (:severidad IS NULL OR a.nivelSeveridad = :severidad)
          AND (:personaId IS NULL OR a.personaId = :personaId)
          AND (:tipoOrigen IS NULL OR a.tipoEvaluacionOrigen = :tipoOrigen)
          AND (:desde IS NULL OR a.fechaGeneracion >= :desde)
          AND (:hasta IS NULL OR a.fechaGeneracion <= :hasta)
        """)
    List<AlertaClinica> buscar(
            @Param("estado") EstadoAlerta estado,
            @Param("severidad") SeveridadAlerta severidad,
            @Param("personaId") Long personaId,
            @Param("tipoOrigen") TipoEvaluacionOrigen tipoOrigen,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Sort sort
    );

    /**
     * Busqueda con filtro por codigos de etiquetas.
     * DISTINCT evita duplicados cuando la alerta tiene varias etiquetas
     * que matchean.
     */
    @EntityGraph(attributePaths = {"detalles", "etiquetas"})
    @Query("""
        SELECT DISTINCT a FROM AlertaClinica a
        JOIN a.etiquetas ae
        WHERE (:estado IS NULL OR a.estado = :estado)
          AND (:severidad IS NULL OR a.nivelSeveridad = :severidad)
          AND (:personaId IS NULL OR a.personaId = :personaId)
          AND (:tipoOrigen IS NULL OR a.tipoEvaluacionOrigen = :tipoOrigen)
          AND (:desde IS NULL OR a.fechaGeneracion >= :desde)
          AND (:hasta IS NULL OR a.fechaGeneracion <= :hasta)
          AND ae.etiquetaCodigo IN :codigosEtiquetas
        """)
    List<AlertaClinica> buscarPorEtiquetas(
            @Param("estado") EstadoAlerta estado,
            @Param("severidad") SeveridadAlerta severidad,
            @Param("personaId") Long personaId,
            @Param("tipoOrigen") TipoEvaluacionOrigen tipoOrigen,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("codigosEtiquetas") Collection<String> codigosEtiquetas,
            Sort sort
    );

    @Query("""
        SELECT new upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto.AlertaClinicaResumen(
            a.idAlerta, a.personaId, a.tipoEvaluacionOrigen, a.puntajeTotal,
            a.nivelSeveridad, a.estado, a.fechaGeneracion, SIZE(a.detalles))
        FROM AlertaClinica a
        WHERE (:estado IS NULL OR a.estado = :estado)
          AND (:desde IS NULL OR a.fechaGeneracion >= :desde)
          AND (:hasta IS NULL OR a.fechaGeneracion <= :hasta)
        """)
    List<AlertaClinicaResumen> buscarResumen(
            @Param("estado") EstadoAlerta estado,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Sort sort
    );

    @Query("""
        SELECT new upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto.AlertaAgregado(
            COUNT(a),
            COALESCE(SUM(CASE WHEN a.nivelSeveridad = 'CRITICA' THEN 1 ELSE 0 END), 0L),
            COALESCE(SUM(CASE WHEN a.estado = 'ATENDIDA' THEN 1 ELSE 0 END), 0L))
        FROM AlertaClinica a
        WHERE (:desde IS NULL OR a.fechaGeneracion >= :desde)
          AND (:hasta IS NULL OR a.fechaGeneracion <= :hasta)
        """)
    AlertaAgregado agregados(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );
}
