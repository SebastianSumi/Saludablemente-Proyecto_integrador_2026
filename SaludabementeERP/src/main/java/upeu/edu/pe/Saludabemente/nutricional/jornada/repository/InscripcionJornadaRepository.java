package upeu.edu.pe.Saludabemente.nutricional.jornada.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.nutricional.jornada.entity.InscripcionJornada;
import upeu.edu.pe.Saludabemente.nutricional.jornada.enums.EstadoInscripcion;

import java.util.List;
import java.util.Optional;

public interface InscripcionJornadaRepository extends JpaRepository<InscripcionJornada, Long> {

    Optional<InscripcionJornada> findByIdJornadaAndIdPersona(Long idJornada, Long idPersona);

    List<InscripcionJornada> findByIdJornadaOrderByNumeroTurnoAsc(Long idJornada);

    List<InscripcionJornada> findByIdPersonaOrderByFechaInscripcionDesc(Long idPersona);

    long countByIdJornadaAndEstado(Long idJornada, EstadoInscripcion estado);

    /**
     * Cuenta los turnos que ocupan cupo: activos y quienes ya asistieron.
     * Las inscripciones canceladas liberan su cupo.
     */
    long countByIdJornadaAndEstadoIn(
            Long idJornada, java.util.Collection<EstadoInscripcion> estados);

    /**
     * El máximo incluye también inscripciones canceladas para no reutilizar
     * números de turno ya entregados.
     */
    @Query("""
            SELECT COALESCE(MAX(i.numeroTurno), 0)
              FROM InscripcionJornada i
             WHERE i.idJornada = :idJornada
            """)
    Integer findMaxNumeroTurno(@Param("idJornada") Long idJornada);
}
