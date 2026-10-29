package upeu.edu.pe.Saludabemente.actividades.agenda.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.actividades.agenda.entity.Actividad;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    List<Actividad> findByIdEquipoActividadOrderByFechaActividadDescIdActividadDesc(Long idEquipo);

    @Query("SELECT a FROM Actividad a WHERE LOWER(a.nombre) LIKE LOWER(CONCAT('%', :q, '%')) ORDER BY a.fechaActividad DESC, a.idActividad DESC")
    List<Actividad> buscarParaSocial(@Param("q") String query, org.springframework.data.domain.Pageable pageable);

    /**
     * Actividades entre dos fechas (para el calendario mensual).
     */
    @Query("""
        SELECT a FROM Actividad a
        WHERE a.fechaActividad BETWEEN :inicio AND :fin
        ORDER BY a.fechaActividad ASC, a.horaInicio ASC
    """)
    List<Actividad> findBetweenDates(@Param("inicio") LocalDate inicio,
                                     @Param("fin") LocalDate fin);

    /**
     * Actividades del dia que no esten canceladas ni finalizadas.
     */
    @Query("""
        SELECT a FROM Actividad a
        WHERE a.fechaActividad = :fecha
          AND a.estado IN ('PROGRAMADA', 'EN_CURSO')
        ORDER BY a.horaInicio ASC
    """)
    List<Actividad> findActivasDelDia(@Param("fecha") LocalDate fecha);

    @Query("""
    SELECT COUNT(a) FROM Actividad a
    WHERE a.lugar = :lugar
      AND a.fechaActividad = :fecha
      AND a.estado <> 'CANCELADA'
      AND (a.horaInicio < :fin AND a.horaFin > :inicio)
""")
    long countConflictoEspacio(@Param("lugar") String lugar,
                               @Param("fecha") LocalDate fecha,
                               @Param("inicio") LocalTime inicio,
                               @Param("fin") LocalTime fin);

    @Query("""
    SELECT COUNT(a) FROM Actividad a
    WHERE a.idUsuarioCoordinador = :idUsuario
      AND a.fechaActividad = :fecha
      AND a.estado <> 'CANCELADA'
      AND (a.horaInicio < :fin AND a.horaFin > :inicio)
""")
    long countConflictoCoordinador(@Param("idUsuario") Long idUsuario,
                                   @Param("fecha") LocalDate fecha,
                                   @Param("inicio") LocalTime inicio,
                                   @Param("fin") LocalTime fin);

    @Query("""
    SELECT COUNT(a) FROM Actividad a
    WHERE a.lugar = :lugar
      AND a.fechaActividad = :fecha
      AND a.idActividad <> :idExcluir
      AND a.estado <> 'CANCELADA'
      AND (a.horaInicio < :fin AND a.horaFin > :inicio)
""")
    long countConflictoEspacioExcluyendo(@Param("lugar") String lugar,
                                         @Param("fecha") LocalDate fecha,
                                         @Param("inicio") LocalTime inicio,
                                         @Param("fin") LocalTime fin,
                                         @Param("idExcluir") Long idExcluir);

    @Query("""
    SELECT COUNT(a) FROM Actividad a
    WHERE a.idUsuarioCoordinador = :idUsuario
      AND a.fechaActividad = :fecha
      AND a.idActividad <> :idExcluir
      AND a.estado <> 'CANCELADA'
      AND (a.horaInicio < :fin AND a.horaFin > :inicio)
""")
    long countConflictoCoordinadorExcluyendo(@Param("idUsuario") Long idUsuario,
                                             @Param("fecha") LocalDate fecha,
                                             @Param("inicio") LocalTime inicio,
                                             @Param("fin") LocalTime fin,
                                             @Param("idExcluir") Long idExcluir);


    List<Actividad> findByEstado(String estado);

    @Query("""
    SELECT a FROM Actividad a
    WHERE a.estado = 'FINALIZADA'
      AND (a.cierreAsistenciasRealizado IS NULL OR a.cierreAsistenciasRealizado = false)
    ORDER BY a.fechaActividad ASC
""")
    List<Actividad> findFinalizadasNoCerradas();
}
