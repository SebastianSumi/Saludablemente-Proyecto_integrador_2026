package upeu.edu.pe.Saludabemente.catalogo.area.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AreaRequest(
        @NotBlank(message = "El nombre del área es obligatorio")
        @Size(max = 100, message = "El nombre del área no puede superar 100 caracteres")
        String nombre,

        @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
        String descripcion
) {
}
