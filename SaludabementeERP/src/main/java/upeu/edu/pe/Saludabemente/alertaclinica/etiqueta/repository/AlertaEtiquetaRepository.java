package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.AlertaResumenParaEtiquetaDto;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.EtiquetaAgregadoDto;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.EtiquetaAsignadaDto;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.entity.AlertaEtiqueta;

import java.time.LocalDateTime;
import java.util.List;

public interface AlertaEtiquetaRepository extends JpaRepository<AlertaEtiqueta, Long> {

    @Query("select ae from AlertaEtiqueta ae join fetch ae.alerta")
    List<AlertaEtiqueta> findAllParaReporte();

    List<AlertaEtiqueta> findByIdEtiqueta(Long idEtiqueta);

    @Query("""
        SELECT new upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.EtiquetaAsignadaDto(
            ae.idEtiqueta,
            ae.etiquetaCodigo,
            ae.etiquetaNombre,
            ae.etiquetaColorHex,
            ae.asignadaPor,
            ae.fechaAsignacion)
        FROM AlertaEtiqueta ae
        WHERE ae.alerta.idAlerta = :idAlerta
        ORDER BY ae.etiquetaCodigo ASC
        """)
    List<EtiquetaAsignadaDto> findEtiquetasByAlerta(@Param("idAlerta") Long idAlerta);

    /**
     * Alertas asociadas a una etiqueta (para el detalle embebido en la
     * respuesta de etiqueta, patron BomERP).
     */
    @Query("""
        SELECT new upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.AlertaResumenParaEtiquetaDto(
            ae.alerta.idAlerta,
            ae.alerta.personaId,
            ae.alerta.nivelSeveridad,
            ae.alerta.estado,
            ae.alerta.fechaGeneracion,
            ae.asignadaPor,
            ae.fechaAsignacion)
        FROM AlertaEtiqueta ae
        WHERE ae.idEtiqueta = :idEtiqueta
        ORDER BY ae.alerta.fechaGeneracion DESC
        """)
    List<AlertaResumenParaEtiquetaDto> findAlertasByEtiqueta(@Param("idEtiqueta") Long idEtiqueta);

    boolean existsByAlerta_IdAlertaAndIdEtiqueta(Long idAlerta, Long idEtiqueta);

    void deleteByAlerta_IdAlertaAndIdEtiqueta(Long idAlerta, Long idEtiqueta);

    /**
     * Agregacion para el dashboard: cuantas veces aparece cada etiqueta
     * en las alertas, con filtro opcional de rango de fechas.
     *
     * Agrupa por (codigo, nombre, color) de la fotocopia. Si en el futuro
     * se renombra una etiqueta, apareceran dos filas (una por cada nombre
     * historico). Es intencional: refleja fielmente el uso historico.
     */
    @Query("""
        SELECT new upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.EtiquetaAgregadoDto(
            ae.etiquetaCodigo,
            ae.etiquetaNombre,
            ae.etiquetaColorHex,
            COUNT(ae))
        FROM AlertaEtiqueta ae
        WHERE (:desde IS NULL OR ae.fechaAsignacion >= :desde)
          AND (:hasta IS NULL OR ae.fechaAsignacion <= :hasta)
        GROUP BY ae.etiquetaCodigo, ae.etiquetaNombre, ae.etiquetaColorHex
        ORDER BY COUNT(ae) DESC, ae.etiquetaCodigo ASC
        """)
    List<EtiquetaAgregadoDto> countAlertasPorEtiqueta(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );
}
