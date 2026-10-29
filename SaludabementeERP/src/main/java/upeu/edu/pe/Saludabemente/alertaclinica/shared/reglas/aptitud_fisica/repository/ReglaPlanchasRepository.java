package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.entity.ReglaPlanchas;

import java.util.List;

public interface ReglaPlanchasRepository extends JpaRepository<ReglaPlanchas, Long> {

    List<ReglaPlanchas> findByEstado(EstadoRegla estado);

    List<ReglaPlanchas> findByEstadoAndSexo(EstadoRegla estado, SexoBiologico sexo);

    List<ReglaPlanchas> findBySexo(SexoBiologico sexo);

    @Query("""
        SELECT r FROM ReglaPlanchas r
        WHERE r.estado = 'ACTIVO'
          AND r.sexo = :sexo
          AND r.edadMin <= :edad
          AND r.edadMax >= :edad
        """)
    List<ReglaPlanchas> findReglasActivasPorDemografia(
            @Param("sexo") SexoBiologico sexo,
            @Param("edad") Integer edad
    );

    @Query("""
        SELECT r FROM ReglaPlanchas r
        WHERE r.estado = 'ACTIVO'
          AND r.sexo = :sexo
          AND r.edadMin <= :edadMax
          AND r.edadMax >= :edadMin
          AND r.repeticionesMin <= :valorMax
          AND r.repeticionesMax >= :valorMin
          AND (:idExcluir IS NULL OR r.idReglaPlanchas <> :idExcluir)
        """)
    List<ReglaPlanchas> findSolapadas(
            @Param("sexo") SexoBiologico sexo,
            @Param("edadMin") Integer edadMin,
            @Param("edadMax") Integer edadMax,
            @Param("valorMin") Integer valorMin,
            @Param("valorMax") Integer valorMax,
            @Param("idExcluir") Long idExcluir
    );
}