package upeu.edu.pe.Saludabemente.recomendacionesia.core.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.entity.EstadoPendiente;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.entity.RecomendacionPendiente;

import java.util.List;

public interface RecomendacionPendienteRepository extends JpaRepository<RecomendacionPendiente, Long> {

    boolean existsByIdPersonaAndIdEvaluacionAndEstadoIn(
            Long idPersona, Long idEvaluacion, List<EstadoPendiente> estados);

    List<RecomendacionPendiente> findByEstadoOrderByFechaCreacionAsc(
            EstadoPendiente estado, Pageable pageable);
}