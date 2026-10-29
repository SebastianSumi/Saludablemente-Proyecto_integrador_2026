package upeu.edu.pe.Saludabemente.nutricional.jornada.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.nutricional.jornada.entity.JornadaEvaluacion;
import upeu.edu.pe.Saludabemente.nutricional.jornada.enums.EstadoJornada;

import java.util.Optional;

public interface JornadaEvaluacionRepository extends JpaRepository<JornadaEvaluacion, Long> {

    /**
     * Bloquea la fila de la jornada durante la inscripción. Así dos
     * solicitudes simultáneas no calculan el mismo turno ni superan
     * la capacidad.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT j FROM JornadaEvaluacion j WHERE j.idJornada = :id")
    Optional<JornadaEvaluacion> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            SELECT j FROM JornadaEvaluacion j
            WHERE (:estado IS NULL OR j.estado = :estado)
              AND (:idPeriodo IS NULL OR j.idPeriodo = :idPeriodo)
            """)
    Page<JornadaEvaluacion> buscar(
            @Param("estado") EstadoJornada estado,
            @Param("idPeriodo") Long idPeriodo,
            Pageable pageable);
}
