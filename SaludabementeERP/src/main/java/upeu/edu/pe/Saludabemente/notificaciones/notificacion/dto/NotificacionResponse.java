package upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionResponse {
    private Long idNotificacion;
    private Long idPersona;
    private String canal;
    private String titulo;
    private String mensaje;
    private String origenModulo;
    private Long idOrigenReferencia;
    private String estadoLectura;
    private String estadoDelivery;
    private LocalDateTime fechaEnvio;
}