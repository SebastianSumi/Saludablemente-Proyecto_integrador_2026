package upeu.edu.pe.Saludabemente.notificaciones.notificacion.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto.BandejaNotificacionDTO;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto.NotificacionResponse;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.entity.Notificacion;
import upeu.edu.pe.Saludabemente.notificaciones.shared.enums.EstadoLectura;

@Mapper(componentModel = "spring")
public interface NotificacionMapper {

    NotificacionResponse toResponse(Notificacion notificacion);

    @Mapping(target = "leido", source = "estadoLectura")
    BandejaNotificacionDTO toBandeja(Notificacion notificacion);

    /**
     * Conversión EstadoLectura -> Boolean para BandejaNotificacionDTO.
     * MapStruct la detecta y la usa automáticamente como método de apoyo
     * para el campo "leido" (Boolean) a partir del enum "estadoLectura".
     */
    default Boolean toLeido(EstadoLectura estadoLectura) {
        return estadoLectura == EstadoLectura.LEIDA;
    }
}