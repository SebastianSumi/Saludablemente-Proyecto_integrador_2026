package upeu.edu.pe.Saludabemente.notificaciones.notificacion.service.notificacion;

import upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto.BandejaNotificacionDTO;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto.NotificacionResponse;

import java.util.List;

public interface NotificacionService {

    List<NotificacionResponse> listar();

    NotificacionResponse obtener(Long id);

    List<BandejaNotificacionDTO> listarBandeja(Long idPersona);

    long contarNoLeidas(Long idPersona);

    void marcarLeida(Long idNotificacion);

    void archivar(Long idNotificacion);
}