package upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import upeu.edu.pe.Saludabemente.notificaciones.shared.enums.CanalNotificacion;

@Getter
@Setter
public class NotificacionRequest {

    @NotNull
    private Long idPersona;

    @NotNull
    private CanalNotificacion canal;

    private String titulo;

    private String mensaje;

    private String origenModulo;

    private Long idOrigenReferencia;
}