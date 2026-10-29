package upeu.edu.pe.Saludabemente.metas.meta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import upeu.edu.pe.Saludabemente.metas.meta.entity.MetaEntity;
import upeu.edu.pe.Saludabemente.metas.shared.enums.EstadoMeta;
import upeu.edu.pe.Saludabemente.metas.shared.enums.TipoOrigen;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MetaRepository extends JpaRepository<MetaEntity, Long> {

    /** Metas de una persona en un estado concreto. */
    List<MetaEntity> findByIdPersonaAndEstado(Long idPersona, EstadoMeta estado);

    /** Todas las metas de una persona. */
    List<MetaEntity> findByIdPersona(Long idPersona);

    /** Todas las metas en un estado concreto. */
    List<MetaEntity> findByEstado(EstadoMeta estado);

    /** Meta por persona, indicador y estado (para detectar duplicados). */
    List<MetaEntity> findByIdPersonaAndIndicadorAndEstado(
            Long idPersona, String indicador, EstadoMeta estado);

    /**
     * Metas ACTIVAS cuya fechaObjetivo ya venció.
     * Usado por el scheduler para marcarlas ABANDONADA.
     */
    @Query("""
        SELECT m FROM MetaEntity m
         WHERE m.estado = :estado
           AND m.fechaObjetivo < :hoy
    """)
    List<MetaEntity> findActivasVencidas(
            @Param("estado") EstadoMeta estado,
            @Param("hoy") LocalDate hoy);

    /**
     * Metas CLINICA ACTIVAS sin ningún check-in desde la fecha indicada.
     * Usado por el scheduler para marcarlas DESVIADA.
     */
    @Query("""
        SELECT m FROM MetaEntity m
         WHERE m.estado = :estado
           AND m.tipoOrigen = :tipoOrigen
           AND NOT EXISTS (
                SELECT 1 FROM CheckInEntity c
                 WHERE c.meta = m
                   AND c.fecha >= :desde
           )
    """)
    List<MetaEntity> findClinicasSinCheckInDesde(
            @Param("estado") EstadoMeta estado,
            @Param("tipoOrigen") TipoOrigen tipoOrigen,
            @Param("desde") LocalDate desde);

    /** Conteo por persona y estado — usado por `MetaQueryApi`. */
    long countByIdPersonaAndEstado(Long idPersona, EstadoMeta estado);

    /** ¿Existe al menos una meta clínica desviada para esta persona? */
    boolean existsByIdPersonaAndTipoOrigenAndEstado(
            Long idPersona, TipoOrigen tipoOrigen, EstadoMeta estado);
}