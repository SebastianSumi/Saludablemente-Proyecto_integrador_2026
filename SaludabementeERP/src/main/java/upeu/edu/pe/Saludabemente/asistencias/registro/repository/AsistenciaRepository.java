package upeu.edu.pe.Saludabemente.asistencias.registro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import upeu.edu.pe.Saludabemente.asistencias.enums.EstadoAsistencia;
import upeu.edu.pe.Saludabemente.asistencias.enums.TipoAsistencia;
import upeu.edu.pe.Saludabemente.asistencias.registro.entity.Asistencia;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    List<Asistencia> findByIdActividad(Long idActividad);

    List<Asistencia> findByIdPersona(Long idPersona);

    Optional<Asistencia> findByIdLocal(String idLocal);

    boolean existsByIdLocal(String idLocal);


    List<Asistencia> findByIdPersonaInAndHoraMarcadoBetween(
            List<Long> idsPersona, LocalDateTime desde, LocalDateTime hasta);

    List<Asistencia> findByIdActividadAndEstado(Long idActividad, EstadoAsistencia estado);

    long countByIdActividadAndEstado(Long idActividad, EstadoAsistencia estado);

    List<Asistencia> findByIdLocalIn(Collection<String> idsLocal);

    List<Asistencia> findByIdActividadAndIdPersonaIn(Long idActividad, Collection<Long> idsPersona);

    List<Asistencia> findByIdActividadAndTipoAsistencia(Long idActividad, TipoAsistencia tipoAsistencia);

    boolean existsByIdActividadAndIdPersona(Long idActividad, Long idPersona);

    boolean existsByIdActividadAndIdPersonaAndTipoAsistencia(
            Long idActividad, Long idPersona, TipoAsistencia tipoAsistencia);

    Optional<Asistencia> findByIdActividadAndIdPersonaAndTipoAsistencia(
            Long idActividad, Long idPersona, TipoAsistencia tipoAsistencia);
}