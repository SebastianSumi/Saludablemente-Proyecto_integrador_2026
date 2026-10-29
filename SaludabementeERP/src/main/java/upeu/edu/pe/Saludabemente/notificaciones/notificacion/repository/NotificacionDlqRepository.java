package upeu.edu.pe.Saludabemente.notificaciones.notificacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.entity.NotificacionDlq;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificacionDlqRepository extends JpaRepository<NotificacionDlq, Long> {

    List<NotificacionDlq> findByResueltoFalseAndProximoIntentoBeforeAndIntentosLessThan(
            LocalDateTime ahora, Integer maxIntentos);

    long countByResueltoFalse();
}