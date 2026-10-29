package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaPerimetroAbdominal;

import java.math.BigDecimal;
import java.util.List;

public interface ReglaPerimetroAbdominalRepository extends JpaRepository<ReglaPerimetroAbdominal, Long> {

    List<ReglaPerimetroAbdominal> findByEstado(EstadoRegla estado);

    List<ReglaPerimetroAbdominal> findByEstadoAndSexo(EstadoRegla estado, SexoBiologico sexo);

    List<ReglaPerimetroAbdominal> findBySexo(SexoBiologico sexo);

    @Query("""
        SELECT r FROM ReglaPerimetroAbdominal r
        WHERE r.estado = 'ACTIVO'
          AND r.sexo = :sexo
          AND r.edadMin <= :edad
          AND r.edadMax >= :edad
        """)
    List<ReglaPerimetroAbdominal> findReglasActivasPorDemografia(
            @Param("sexo") SexoBiologico sexo,
            @Param("edad") Integer edad
    );

    @Query("""
        SELECT r FROM ReglaPerimetroAbdominal r
        WHERE r.estado = 'ACTIVO'
          AND r.sexo = :sexo
          AND r.edadMin <= :edadMax
          AND r.edadMax >= :edadMin
          AND r.perimetroCmMin <= :valorMax
          AND r.perimetroCmMax >= :valorMin
          AND (:idExcluir IS NULL OR r.idReglaPerimetro <> :idExcluir)
        """)
    List<ReglaPerimetroAbdominal> findSolapadas(
            @Param("sexo") SexoBiologico sexo,
            @Param("edadMin") Integer edadMin,
            @Param("edadMax") Integer edadMax,
            @Param("valorMin") BigDecimal valorMin,
            @Param("valorMax") BigDecimal valorMax,
            @Param("idExcluir") Long idExcluir
    );
}