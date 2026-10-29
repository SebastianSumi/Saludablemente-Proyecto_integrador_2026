package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaGrasaVisceral;

import java.math.BigDecimal;
import java.util.List;

public interface ReglaGrasaVisceralRepository extends JpaRepository<ReglaGrasaVisceral, Long> {

    List<ReglaGrasaVisceral> findByEstado(EstadoRegla estado);

    List<ReglaGrasaVisceral> findByEstadoAndSexo(EstadoRegla estado, SexoBiologico sexo);

    List<ReglaGrasaVisceral> findBySexo(SexoBiologico sexo);

    @Query("""
        SELECT r FROM ReglaGrasaVisceral r
        WHERE r.estado = 'ACTIVO'
          AND r.sexo = :sexo
          AND r.edadMin <= :edad
          AND r.edadMax >= :edad
        """)
    List<ReglaGrasaVisceral> findReglasActivasPorDemografia(
            @Param("sexo") SexoBiologico sexo,
            @Param("edad") Integer edad
    );

    @Query("""
        SELECT r FROM ReglaGrasaVisceral r
        WHERE r.estado = 'ACTIVO'
          AND r.sexo = :sexo
          AND r.edadMin <= :edadMax
          AND r.edadMax >= :edadMin
          AND r.nivelMin <= :valorMax
          AND r.nivelMax >= :valorMin
          AND (:idExcluir IS NULL OR r.idReglaVisceral <> :idExcluir)
        """)
    List<ReglaGrasaVisceral> findSolapadas(
            @Param("sexo") SexoBiologico sexo,
            @Param("edadMin") Integer edadMin,
            @Param("edadMax") Integer edadMax,
            @Param("valorMin") BigDecimal valorMin,
            @Param("valorMax") BigDecimal valorMax,
            @Param("idExcluir") Long idExcluir
    );
}