package upeu.edu.pe.Saludabemente.notificaciones.notificacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.entity.Notificacion;
import upeu.edu.pe.Saludabemente.notificaciones.shared.enums.EstadoLectura;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByIdPersonaOrderByFechaEnvioDesc(Long idPersona);

    List<Notificacion> findByIdPersonaAndEstadoLecturaOrderByFechaEnvioDesc(
            Long idPersona, EstadoLectura estadoLectura);

    List<Notificacion> findByIdPersonaAndEstadoLecturaNotOrderByFechaEnvioDesc(
            Long idPersona, EstadoLectura estadoLectura);

    long countByIdPersonaAndEstadoLectura(Long idPersona, EstadoLectura estadoLectura);
}