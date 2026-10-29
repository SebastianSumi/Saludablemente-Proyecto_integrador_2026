package upeu.edu.pe.Saludabemente.nutricional.bioquimico.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.entity.DetalleBioquimico;

import java.util.List;
import java.util.Optional;

public interface BioquimicoRepository extends JpaRepository<DetalleBioquimico, Long> {

    Optional<DetalleBioquimico> findByEvaluacion_IdEvaluacion(Long idEvaluacion);

    boolean existsByEvaluacion_IdEvaluacion(Long idEvaluacion);

    @Modifying
    @Query("DELETE FROM DetalleBioquimico b WHERE b.archivoOrigen = :archivo")
    int deleteByArchivoOrigen(@Param("archivo") String archivoOrigen);

    @Query("""
            SELECT b.dxBioquimico, COUNT(b)
              FROM DetalleBioquimico b
              JOIN b.evaluacion e
             WHERE e.idPeriodo = :periodo
             GROUP BY b.dxBioquimico
            """)
    List<Object[]> contarPorRiesgoGlobal(@Param("periodo") String periodo);
}