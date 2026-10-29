package upeu.edu.pe.Saludabemente.teams.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TeamRequest(
        @NotBlank(message = "El nombre del equipo es obligatorio")
        @Size(max = 100, message = "El nombre del equipo no puede superar 100 caracteres")
        String nombre,

        @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
        String descripcion,
        @jakarta.validation.constraints.Pattern(regexp = "#[0-9a-fA-F]{6}") String color,
        @Size(max = 150) String slogan,
        @Size(max = 100) String referenciaBiblica,
        @Size(max = 1000) String textoVersiculo
) {
    public TeamRequest(String nombre, String descripcion) {
        this(nombre, descripcion, null, null, null, null);
    }
}
