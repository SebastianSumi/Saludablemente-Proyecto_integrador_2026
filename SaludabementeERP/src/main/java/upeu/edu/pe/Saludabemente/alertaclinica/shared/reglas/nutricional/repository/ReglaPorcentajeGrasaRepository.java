package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaPorcentajeGrasa;

import java.math.BigDecimal;
import java.util.List;

public interface ReglaPorcentajeGrasaRepository extends JpaRepository<ReglaPorcentajeGrasa, Long> {

    List<ReglaPorcentajeGrasa> findByEstado(EstadoRegla estado);

    List<ReglaPorcentajeGrasa> findByEstadoAndSexo(EstadoRegla estado, SexoBiologico sexo);

    List<ReglaPorcentajeGrasa> findBySexo(SexoBiologico sexo);

    @Query("""
        SELECT r FROM ReglaPorcentajeGrasa r
        WHERE r.estado = 'ACTIVO'
          AND r.sexo = :sexo
          AND r.edadMin <= :edad
          AND r.edadMax >= :edad
        """)
    List<ReglaPorcentajeGrasa> findReglasActivasPorDemografia(
            @Param("sexo") SexoBiologico sexo,
            @Param("edad") Integer edad
    );

    @Query("""
        SELECT r FROM ReglaPorcentajeGrasa r
        WHERE r.estado = 'ACTIVO'
          AND r.sexo = :sexo
          AND r.edadMin <= :edadMax
          AND r.edadMax >= :edadMin
          AND r.porcentajeMin <= :valorMax
          AND r.porcentajeMax >= :valorMin
          AND (:idExcluir IS NULL OR r.idReglaGrasa <> :idExcluir)
        """)
    List<ReglaPorcentajeGrasa> findSolapadas(
            @Param("sexo") SexoBiologico sexo,
            @Param("edadMin") Integer edadMin,
            @Param("edadMax") Integer edadMax,
            @Param("valorMin") BigDecimal valorMin,
            @Param("valorMax") BigDecimal valorMax,
            @Param("idExcluir") Long idExcluir
    );
}