package upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.entity.DetalleFuerzaResistencia;

import java.util.Optional;

public interface FuerzaRepository extends JpaRepository<DetalleFuerzaResistencia, Long> {
    Optional<DetalleFuerzaResistencia> findByEvaluacionId(Long idEvaluacion);
}