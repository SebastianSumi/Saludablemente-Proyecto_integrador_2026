package upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import upeu.edu.pe.Saludabemente.notificaciones.shared.enums.CanalNotificacion;

@Getter
@Setter
public class PlantillaRequest {

    @NotBlank
    @Size(max = 60)
    private String codigoEvento;

    @NotNull
    private CanalNotificacion canal;

    @Size(max = 200)
    private String asunto;

    @NotBlank
    private String cuerpoHtml;

    private Boolean activo;
}