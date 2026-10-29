package upeu.edu.pe.Saludabemente.metas.meta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import upeu.edu.pe.Saludabemente.metas.meta.entity.CheckInEntity;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CheckInRepository extends JpaRepository<CheckInEntity, Long> {

    /** Anti-duplicado diario: ¿ya existe check-in para esa meta en esa fecha? */
    boolean existsByMetaIdAndFecha(Long metaId, LocalDate fecha);

    /** Historial de check-ins de una meta, más recientes primero. */
    List<CheckInEntity> findByMetaIdOrderByFechaDesc(Long metaId);

    /** Cantidad de check-ins de una meta. */
    long countByMetaId(Long metaId);

    /**
     * Fechas de check-ins válidos (no marcados como NO logrado) de una meta,
     * ordenadas descendentemente. Base para el cálculo de racha.
     */
    @Query("""
        SELECT c.fecha FROM CheckInEntity c
         WHERE c.meta.id = :metaId
           AND (c.logrado IS NULL OR c.logrado = true)
         ORDER BY c.fecha DESC
    """)
    List<LocalDate> findFechasValidasDesc(@Param("metaId") Long metaId);

    /** Último check-in de una meta, o null si nunca registró. */
    @Query("SELECT MAX(c.fecha) FROM CheckInEntity c WHERE c.meta.id = :metaId")
    LocalDate findUltimaFecha(@Param("metaId") Long metaId);
}