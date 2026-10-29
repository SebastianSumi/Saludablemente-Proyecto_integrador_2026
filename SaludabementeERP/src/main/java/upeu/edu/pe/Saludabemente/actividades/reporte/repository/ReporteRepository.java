package upeu.edu.pe.Saludabemente.actividades.reporte.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import upeu.edu.pe.Saludabemente.actividades.agenda.entity.Actividad;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio de solo lectura para reportes.
 * No tiene entidad propia: usa EntityManager para queries de agregacion.
 */
@Repository
public class ReporteRepository {

    @PersistenceContext
    private EntityManager em;

    /**
     * Actividades entre dos fechas ordenadas cronologicamente.
     */
    public List<Actividad> findActividadesPorPeriodo(LocalDate inicio, LocalDate fin) {
        return em.createQuery("""
                SELECT a FROM Actividad a
                WHERE a.fechaActividad BETWEEN :inicio AND :fin
                ORDER BY a.fechaActividad ASC, a.horaInicio ASC
            """, Actividad.class)
                .setParameter("inicio", inicio)
                .setParameter("fin", fin)
                .getResultList();
    }

    /**
     * Conteo por estado en un periodo (para la cabecera del Reporte B).
     */
    public long contarPorEstado(LocalDate inicio, LocalDate fin, String estado) {
        return em.createQuery("""
                SELECT COUNT(a) FROM Actividad a
                WHERE a.fechaActividad BETWEEN :inicio AND :fin
                  AND a.estado = :estado
            """, Long.class)
                .setParameter("inicio", inicio)
                .setParameter("fin", fin)
                .setParameter("estado", estado)
                .getSingleResult();
    }
}