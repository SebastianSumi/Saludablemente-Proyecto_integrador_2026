package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaMasaMuscular;

import java.math.BigDecimal;
import java.util.List;

public interface ReglaMasaMuscularRepository extends JpaRepository<ReglaMasaMuscular, Long> {

    List<ReglaMasaMuscular> findByEstado(EstadoRegla estado);

    List<ReglaMasaMuscular> findByEstadoAndSexo(EstadoRegla estado, SexoBiologico sexo);

    List<ReglaMasaMuscular> findBySexo(SexoBiologico sexo);

    @Query("""
        SELECT r FROM ReglaMasaMuscular r
        WHERE r.estado = 'ACTIVO'
          AND r.sexo = :sexo
          AND r.edadMin <= :edad
          AND r.edadMax >= :edad
        """)
    List<ReglaMasaMuscular> findReglasActivasPorDemografia(
            @Param("sexo") SexoBiologico sexo,
            @Param("edad") Integer edad
    );

    @Query("""
        SELECT r FROM ReglaMasaMuscular r
        WHERE r.estado = 'ACTIVO'
          AND r.sexo = :sexo
          AND r.edadMin <= :edadMax
          AND r.edadMax >= :edadMin
          AND r.porcentajeMin <= :valorMax
          AND r.porcentajeMax >= :valorMin
          AND (:idExcluir IS NULL OR r.idReglaMusculo <> :idExcluir)
        """)
    List<ReglaMasaMuscular> findSolapadas(
            @Param("sexo") SexoBiologico sexo,
            @Param("edadMin") Integer edadMin,
            @Param("edadMax") Integer edadMax,
            @Param("valorMin") BigDecimal valorMin,
            @Param("valorMax") BigDecimal valorMax,
            @Param("idExcluir") Long idExcluir
    );
}