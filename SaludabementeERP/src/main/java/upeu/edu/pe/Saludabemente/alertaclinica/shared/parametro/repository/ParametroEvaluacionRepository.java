package upeu.edu.pe.Saludabemente.alertaclinica.shared.parametro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.parametro.entity.ParametroEvaluacion;

import java.util.List;
import java.util.Optional;

public interface ParametroEvaluacionRepository extends JpaRepository<ParametroEvaluacion, Long> {

    Optional<ParametroEvaluacion> findByCodigo(String codigo);

    List<ParametroEvaluacion> findByEstado(String estado);

    boolean existsByCodigo(String codigo);
}
