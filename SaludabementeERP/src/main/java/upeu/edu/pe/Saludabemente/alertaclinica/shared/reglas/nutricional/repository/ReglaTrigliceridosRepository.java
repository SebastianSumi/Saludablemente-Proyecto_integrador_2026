package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaTrigliceridos;

import java.math.BigDecimal;
import java.util.List;

public interface ReglaTrigliceridosRepository extends JpaRepository<ReglaTrigliceridos, Long> {

    List<ReglaTrigliceridos> findByEstado(EstadoRegla estado);

    List<ReglaTrigliceridos> findByEstadoAndSexo(EstadoRegla estado, SexoBiologico sexo);

    List<ReglaTrigliceridos> findBySexo(SexoBiologico sexo);

    @Query("""
        SELECT r FROM ReglaTrigliceridos r
        WHERE r.estado = 'ACTIVO'
          AND r.sexo = :sexo
          AND r.edadMin <= :edad
          AND r.edadMax >= :edad
        """)
    List<ReglaTrigliceridos> findReglasActivasPorDemografia(
            @Param("sexo") SexoBiologico sexo,
            @Param("edad") Integer edad
    );

    @Query("""
        SELECT r FROM ReglaTrigliceridos r
        WHERE r.estado = 'ACTIVO'
          AND r.sexo = :sexo
          AND r.edadMin <= :edadMax
          AND r.edadMax >= :edadMin
          AND r.trigliceridosMgDlMin <= :valorMax
          AND r.trigliceridosMgDlMax >= :valorMin
          AND (:idExcluir IS NULL OR r.idReglaTrigliceridos <> :idExcluir)
        """)
    List<ReglaTrigliceridos> findSolapadas(
            @Param("sexo") SexoBiologico sexo,
            @Param("edadMin") Integer edadMin,
            @Param("edadMax") Integer edadMax,
            @Param("valorMin") BigDecimal valorMin,
            @Param("valorMax") BigDecimal valorMax,
            @Param("idExcluir") Long idExcluir
    );
}