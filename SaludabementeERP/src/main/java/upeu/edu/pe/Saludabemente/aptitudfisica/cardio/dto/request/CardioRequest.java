package upeu.edu.pe.Saludabemente.aptitudfisica.cardio.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CardioRequest(
        @NotNull @Min(0) @Max(200) Integer repeticionesSoga30s,
        @NotNull @Min(30) @Max(900) Integer tiempoCarrera400mSeg
) {}