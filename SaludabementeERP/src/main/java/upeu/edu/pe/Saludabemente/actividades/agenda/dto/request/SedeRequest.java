package upeu.edu.pe.Saludabemente.actividades.agenda.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SedeRequest(
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitudOficial,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitudOficial,
        @Min(10) @Max(2000) Integer toleranciaGeocercaMetros
) {}
