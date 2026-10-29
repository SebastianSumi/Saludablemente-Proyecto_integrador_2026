package upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FuerzaRequest(
        @NotNull @Min(0) @Max(100) Integer repeticionesAbdominales,
        @NotNull @Min(0) @Max(100) Integer repeticionesFlexionBrazo,
        @NotNull @DecimalMin("0.0") @DecimalMax("400.0") BigDecimal saltoLongitudCm
) {}