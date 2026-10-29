package upeu.edu.pe.Saludabemente.asistencias.cuarentena.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import upeu.edu.pe.Saludabemente.asistencias.cuarentena.entity.AsistenciaCuarentena;
import upeu.edu.pe.Saludabemente.asistencias.enums.EstadoCuarentena;
import upeu.edu.pe.Saludabemente.asistencias.enums.MotivoCuarentena;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CuarentenaRepository extends JpaRepository<AsistenciaCuarentena, Long> {

    Optional<AsistenciaCuarentena> findByIdLocal(String idLocal);

    boolean existsByIdLocal(String idLocal);

    List<AsistenciaCuarentena> findByIdLocalIn(Collection<String> idsLocal);

    Page<AsistenciaCuarentena> findByEstado(EstadoCuarentena estado, Pageable pageable);

    @Query("""
        SELECT c FROM AsistenciaCuarentena c
        WHERE (:estado IS NULL OR c.estado = :estado)
          AND (:motivo IS NULL OR c.motivo = :motivo)
          AND (:idActividad IS NULL OR c.idActividad = :idActividad)
          AND (:idPersona IS NULL OR c.idPersona = :idPersona)
          AND (:desde IS NULL OR c.fechaCreacion >= :desde)
          AND (:hasta IS NULL OR c.fechaCreacion <= :hasta)
        ORDER BY c.fechaCreacion DESC
    """)
    Page<AsistenciaCuarentena> buscarConFiltros(
            @Param("estado") EstadoCuarentena estado,
            @Param("motivo") MotivoCuarentena motivo,
            @Param("idActividad") Long idActividad,
            @Param("idPersona") Long idPersona,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable);

    long countByEstado(EstadoCuarentena estado);
}