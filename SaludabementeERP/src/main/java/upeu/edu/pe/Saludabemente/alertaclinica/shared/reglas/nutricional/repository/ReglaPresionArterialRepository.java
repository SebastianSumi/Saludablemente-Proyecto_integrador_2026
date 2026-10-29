package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaPresionArterial;

import java.util.List;

public interface ReglaPresionArterialRepository extends JpaRepository<ReglaPresionArterial, Long> {

    List<ReglaPresionArterial> findByEstado(EstadoRegla estado);

    List<ReglaPresionArterial> findByEstadoAndSexo(EstadoRegla estado, SexoBiologico sexo);

    List<ReglaPresionArterial> findBySexo(SexoBiologico sexo);

    @Query("""
        SELECT r FROM ReglaPresionArterial r
        WHERE r.estado = 'ACTIVO'
          AND r.sexo = :sexo
          AND r.edadMin <= :edad
          AND r.edadMax >= :edad
        """)
    List<ReglaPresionArterial> findReglasActivasPorDemografia(
            @Param("sexo") SexoBiologico sexo,
            @Param("edad") Integer edad
    );

    /**
     * Anti-overlap especial: dos reglas de presion se solapan solo si
     * AMBOS pares de rangos (sistolica Y diastolica) se solapan a la vez.
     */
    @Query("""
        SELECT r FROM ReglaPresionArterial r
        WHERE r.estado = 'ACTIVO'
          AND r.sexo = :sexo
          AND r.edadMin <= :edadMax
          AND r.edadMax >= :edadMin
          AND r.sistolicaMmHgMin <= :sistolicaMax
          AND r.sistolicaMmHgMax >= :sistolicaMin
          AND r.diastolicaMmHgMin <= :diastolicaMax
          AND r.diastolicaMmHgMax >= :diastolicaMin
          AND (:idExcluir IS NULL OR r.idReglaPresion <> :idExcluir)
        """)
    List<ReglaPresionArterial> findSolapadas(
            @Param("sexo") SexoBiologico sexo,
            @Param("edadMin") Integer edadMin,
            @Param("edadMax") Integer edadMax,
            @Param("sistolicaMin") Integer sistolicaMin,
            @Param("sistolicaMax") Integer sistolicaMax,
            @Param("diastolicaMin") Integer diastolicaMin,
            @Param("diastolicaMax") Integer diastolicaMax,
            @Param("idExcluir") Long idExcluir
    );
}