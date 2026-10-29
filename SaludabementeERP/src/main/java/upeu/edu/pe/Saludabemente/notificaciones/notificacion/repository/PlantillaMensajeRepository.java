package upeu.edu.pe.Saludabemente.notificaciones.notificacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.entity.PlantillaMensaje;
import upeu.edu.pe.Saludabemente.notificaciones.shared.enums.CanalNotificacion;

import java.util.Optional;

public interface PlantillaMensajeRepository extends JpaRepository<PlantillaMensaje, Long> {

    Optional<PlantillaMensaje> findByCodigoEventoAndCanalAndActivoTrue(
            String codigoEvento, CanalNotificacion canal);

    Optional<PlantillaMensaje> findByCodigoEvento(String codigoEvento);
}