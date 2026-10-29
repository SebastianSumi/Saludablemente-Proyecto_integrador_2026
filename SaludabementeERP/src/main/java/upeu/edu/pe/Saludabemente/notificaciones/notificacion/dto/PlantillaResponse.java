package upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantillaResponse {
    private Long idPlantilla;
    private String codigoEvento;
    private String canal;
    private String asunto;
    private String cuerpoHtml;
    private Boolean activo;
}