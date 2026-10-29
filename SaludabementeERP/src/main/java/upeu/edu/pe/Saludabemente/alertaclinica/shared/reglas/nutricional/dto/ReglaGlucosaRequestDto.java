package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto;

import jakarta.validation.constraints.*;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.clasificaciones.ClasificacionGlucosa;

import java.math.BigDecimal;

public record ReglaGlucosaRequestDto(

        @NotNull(message = "El sexo es obligatorio")
        SexoBiologico sexo,

        @NotNull(message = "La edad minima es obligatoria")
        @Min(value = 0, message = "La edad minima no puede ser negativa")
        Integer edadMin,

        @NotNull(message = "La edad maxima es obligatoria")
        @Max(value = 120, message = "La edad maxima no es realista")
        Integer edadMax,

        @NotNull(message = "La glucosa minima es obligatoria")
        @DecimalMin(value = "0.00", message = "La glucosa minima no puede ser negativa")
        BigDecimal glucosaMgDlMin,

        @NotNull(message = "La glucosa maxima es obligatoria")
        @DecimalMax(value = "1000.00", message = "La glucosa maxima es irreal")
        BigDecimal glucosaMgDlMax,

        @NotNull(message = "Los puntos de riesgo son obligatorios")
        @Min(value = 0, message = "Los puntos no pueden ser negativos")
        Integer puntosRiesgo,

        @NotNull(message = "La clasificacion es obligatoria")
        ClasificacionGlucosa clasificacion
) {}