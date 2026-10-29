package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.entity.ReglaSaltoSinImpulso;

import java.math.BigDecimal;
import java.util.List;

public interface ReglaSaltoSinImpulsoRepository extends JpaRepository<ReglaSaltoSinImpulso, Long> {

    List<ReglaSaltoSinImpulso> findByEstado(EstadoRegla estado);

    List<ReglaSaltoSinImpulso> findByEstadoAndSexo(EstadoRegla estado, SexoBiologico sexo);

    List<ReglaSaltoSinImpulso> findBySexo(SexoBiologico sexo);

    @Query("""
        SELECT r FROM ReglaSaltoSinImpulso r
        WHERE r.estado = 'ACTIVO'
          AND r.sexo = :sexo
          AND r.edadMin <= :edad
          AND r.edadMax >= :edad
        """)
    List<ReglaSaltoSinImpulso> findReglasActivasPorDemografia(
            @Param("sexo") SexoBiologico sexo,
            @Param("edad") Integer edad
    );

    @Query("""
        SELECT r FROM ReglaSaltoSinImpulso r
        WHERE r.estado = 'ACTIVO'
          AND r.sexo = :sexo
          AND r.edadMin <= :edadMax
          AND r.edadMax >= :edadMin
          AND r.distanciaCmMin <= :valorMax
          AND r.distanciaCmMax >= :valorMin
          AND (:idExcluir IS NULL OR r.idReglaSalto <> :idExcluir)
        """)
    List<ReglaSaltoSinImpulso> findSolapadas(
            @Param("sexo") SexoBiologico sexo,
            @Param("edadMin") Integer edadMin,
            @Param("edadMax") Integer edadMax,
            @Param("valorMin") BigDecimal valorMin,
            @Param("valorMax") BigDecimal valorMax,
            @Param("idExcluir") Long idExcluir
    );
}