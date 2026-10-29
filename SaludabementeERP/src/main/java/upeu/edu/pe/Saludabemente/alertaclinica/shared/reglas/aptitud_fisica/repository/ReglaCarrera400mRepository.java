package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.entity.ReglaCarrera400m;

import java.util.List;

public interface ReglaCarrera400mRepository extends JpaRepository<ReglaCarrera400m, Long> {

    List<ReglaCarrera400m> findByEstado(EstadoRegla estado);

    List<ReglaCarrera400m> findByEstadoAndSexo(EstadoRegla estado, SexoBiologico sexo);

    List<ReglaCarrera400m> findBySexo(SexoBiologico sexo);

    @Query("""
        SELECT r FROM ReglaCarrera400m r
        WHERE r.estado = 'ACTIVO'
          AND r.sexo = :sexo
          AND r.edadMin <= :edad
          AND r.edadMax >= :edad
        """)
    List<ReglaCarrera400m> findReglasActivasPorDemografia(
            @Param("sexo") SexoBiologico sexo,
            @Param("edad") Integer edad
    );

    @Query("""
        SELECT r FROM ReglaCarrera400m r
        WHERE r.estado = 'ACTIVO'
          AND r.sexo = :sexo
          AND r.edadMin <= :edadMax
          AND r.edadMax >= :edadMin
          AND r.tiempoSegundosMin <= :valorMax
          AND r.tiempoSegundosMax >= :valorMin
          AND (:idExcluir IS NULL OR r.idReglaCarrera <> :idExcluir)
        """)
    List<ReglaCarrera400m> findSolapadas(
            @Param("sexo") SexoBiologico sexo,
            @Param("edadMin") Integer edadMin,
            @Param("edadMax") Integer edadMax,
            @Param("valorMin") Integer valorMin,
            @Param("valorMax") Integer valorMax,
            @Param("idExcluir") Long idExcluir
    );
}