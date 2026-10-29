package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaImc;

import java.math.BigDecimal;
import java.util.List;

public interface ReglaImcRepository extends JpaRepository<ReglaImc, Long> {

    /**
     * Catalogo activo aplicable a un paciente segun demografia.
     * Consumido por el motor de alertas.
     */
    @Query("""
        SELECT r FROM ReglaImc r
        WHERE r.estado = 'ACTIVO'
          AND r.sexo = :sexo
          AND r.edadMin <= :edad
          AND r.edadMax >= :edad
        """)
    List<ReglaImc> findReglasActivasPorDemografia(
            @Param("sexo") SexoBiologico sexo,
            @Param("edad") Integer edad
    );

    /**
     * Deteccion de solapamiento para el CRUD.
     * Excluye el propio ID cuando se trata de un PUT.
     */
    @Query("""
        SELECT r FROM ReglaImc r
        WHERE r.estado = 'ACTIVO'
          AND r.sexo = :sexo
          AND r.edadMin <= :edadMax
          AND r.edadMax >= :edadMin
          AND r.imcMin <= :imcMax
          AND r.imcMax >= :imcMin
          AND (:idExcluir IS NULL OR r.idReglaImc <> :idExcluir)
        """)
    List<ReglaImc> findSolapadas(
            @Param("sexo") SexoBiologico sexo,
            @Param("edadMin") Integer edadMin,
            @Param("edadMax") Integer edadMax,
            @Param("imcMin") BigDecimal imcMin,
            @Param("imcMax") BigDecimal imcMax,
            @Param("idExcluir") Long idExcluir
    );

    List<ReglaImc> findBySexo(SexoBiologico sexo);


    List<ReglaImc> findByEstado(EstadoRegla estado);

    List<ReglaImc> findByEstadoAndSexo(EstadoRegla estado, SexoBiologico sexo);
}