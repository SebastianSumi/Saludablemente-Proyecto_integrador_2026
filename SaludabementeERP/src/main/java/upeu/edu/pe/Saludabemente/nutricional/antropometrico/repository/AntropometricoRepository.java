package upeu.edu.pe.Saludabemente.nutricional.antropometrico.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.nutricional.antropometrico.entity.DetalleAntropometrico;

import java.util.List;
import java.util.Optional;

public interface AntropometricoRepository extends JpaRepository<DetalleAntropometrico, Long> {

    Optional<DetalleAntropometrico> findByEvaluacion_IdEvaluacion(Long idEvaluacion);

    boolean existsByEvaluacion_IdEvaluacion(Long idEvaluacion);

    @Query("""
            SELECT a.dxImc, COUNT(a)
              FROM DetalleAntropometrico a
              JOIN a.evaluacion e
             WHERE e.idPeriodo = :periodo
             GROUP BY a.dxImc
            """)
    List<Object[]> contarPorDiagnosticoImc(@Param("periodo") String periodo);
}