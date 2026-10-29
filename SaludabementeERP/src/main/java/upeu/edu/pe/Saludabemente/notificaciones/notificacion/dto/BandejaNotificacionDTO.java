package upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import upeu.edu.pe.Saludabemente.notificaciones.shared.enums.CanalNotificacion;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BandejaNotificacionDTO {
    private Long idNotificacion;
    private String titulo;
    private String mensaje;
    private String origenModulo;
    private Long idOrigenReferencia;
    private CanalNotificacion canal;
    private Boolean leido;
    private LocalDateTime fechaEnvio;
}