package upeu.edu.pe.Saludabemente.asistencias.exoneracion.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import upeu.edu.pe.Saludabemente.asistencias.enums.EstadoJustificacion;
import upeu.edu.pe.Saludabemente.asistencias.exoneracion.entity.Justificacion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JustificacionRepository extends JpaRepository<Justificacion, Long> {

    Optional<Justificacion> findByIdActividadAndIdPersona(Long idActividad, Long idPersona);

    List<Justificacion> findByIdPersonaAndEstado(Long idPersona, EstadoJustificacion estado);

    boolean existsByIdActividadAndIdPersonaAndEstado(
            Long idActividad, Long idPersona, EstadoJustificacion estado);

    @Query("""
        SELECT j FROM Justificacion j
        WHERE (:estado IS NULL OR j.estado = :estado)
          AND (:idPersona IS NULL OR j.idPersona = :idPersona)
          AND (:desde IS NULL OR j.fechaSolicitud >= :desde)
          AND (:hasta IS NULL OR j.fechaSolicitud <= :hasta)
        ORDER BY j.fechaSolicitud DESC
    """)
    Page<Justificacion> buscarConFiltros(
            @Param("estado") EstadoJustificacion estado,
            @Param("idPersona") Long idPersona,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable);
}