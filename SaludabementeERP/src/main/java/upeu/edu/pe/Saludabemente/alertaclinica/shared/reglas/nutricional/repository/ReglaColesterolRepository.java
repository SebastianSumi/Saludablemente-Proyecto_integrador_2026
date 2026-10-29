package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaColesterol;

import java.math.BigDecimal;
import java.util.List;

public interface ReglaColesterolRepository extends JpaRepository<ReglaColesterol, Long> {

    List<ReglaColesterol> findByEstado(EstadoRegla estado);

    List<ReglaColesterol> findByEstadoAndSexo(EstadoRegla estado, SexoBiologico sexo);

    List<ReglaColesterol> findBySexo(SexoBiologico sexo);

    @Query("""
        SELECT r FROM ReglaColesterol r
        WHERE r.estado = 'ACTIVO'
          AND r.sexo = :sexo
          AND r.edadMin <= :edad
          AND r.edadMax >= :edad
        """)
    List<ReglaColesterol> findReglasActivasPorDemografia(
            @Param("sexo") SexoBiologico sexo,
            @Param("edad") Integer edad
    );

    @Query("""
        SELECT r FROM ReglaColesterol r
        WHERE r.estado = 'ACTIVO'
          AND r.sexo = :sexo
          AND r.edadMin <= :edadMax
          AND r.edadMax >= :edadMin
          AND r.colesterolMgDlMin <= :valorMax
          AND r.colesterolMgDlMax >= :valorMin
          AND (:idExcluir IS NULL OR r.idReglaColesterol <> :idExcluir)
        """)
    List<ReglaColesterol> findSolapadas(
            @Param("sexo") SexoBiologico sexo,
            @Param("edadMin") Integer edadMin,
            @Param("edadMax") Integer edadMax,
            @Param("valorMin") BigDecimal valorMin,
            @Param("valorMax") BigDecimal valorMax,
            @Param("idExcluir") Long idExcluir
    );
}