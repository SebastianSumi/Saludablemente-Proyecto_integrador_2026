package upeu.edu.pe.Saludabemente.actividades.operacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelacionRequest(
        @NotBlank @Size(max = 200) String motivo
) {}