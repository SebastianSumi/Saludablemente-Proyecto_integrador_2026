package upeu.edu.pe.Saludabemente.catalogo.area.dto.response;

import lombok.Getter;
import lombok.Setter;

/**
 * Vista reducida de un área de trabajo, embebida en PersonaResponse.
 */
public record AreaResumen(
        Long id,
        String nombre
){
}
