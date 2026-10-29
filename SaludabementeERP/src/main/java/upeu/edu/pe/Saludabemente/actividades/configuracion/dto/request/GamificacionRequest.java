package upeu.edu.pe.Saludabemente.actividades.configuracion.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GamificacionRequest(
        @NotNull @Min(0) Integer puntos
) {}