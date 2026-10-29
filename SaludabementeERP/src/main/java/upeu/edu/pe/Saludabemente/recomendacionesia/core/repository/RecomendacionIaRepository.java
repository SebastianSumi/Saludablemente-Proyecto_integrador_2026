package upeu.edu.pe.Saludabemente.recomendacionesia.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.entity.RecomendacionIa;

import java.util.List;

public interface RecomendacionIaRepository extends JpaRepository<RecomendacionIa, Long> {

    List<RecomendacionIa> findByIdPersonaAndVigente(Long idPersona, Integer vigente);

    boolean existsByIdPersonaAndIdEvaluacionAndVigente(
            Long idPersona, Long idEvaluacion, Integer vigente);

    @Modifying
    @Query("UPDATE RecomendacionIa r SET r.vigente = 0 WHERE r.idPersona = :idPersona AND r.vigente = 1")
    int desactivarVigentesDePersona(@Param("idPersona") Long idPersona);
}