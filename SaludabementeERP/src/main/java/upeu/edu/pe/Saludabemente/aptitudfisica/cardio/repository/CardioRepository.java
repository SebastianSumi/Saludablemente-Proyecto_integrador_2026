package upeu.edu.pe.Saludabemente.aptitudfisica.cardio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.Saludabemente.aptitudfisica.cardio.entity.DetalleCardiovascular;

import java.util.Optional;

public interface CardioRepository extends JpaRepository<DetalleCardiovascular, Long> {
    Optional<DetalleCardiovascular> findByEvaluacionId(Long idEvaluacion);
}