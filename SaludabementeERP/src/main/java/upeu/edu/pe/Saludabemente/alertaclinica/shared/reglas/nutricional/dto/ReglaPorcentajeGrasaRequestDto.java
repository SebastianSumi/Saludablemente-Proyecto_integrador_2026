package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto;

import jakarta.validation.constraints.*;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.clasificaciones.ClasificacionPorcentajeGrasa;

import java.math.BigDecimal;

public record ReglaPorcentajeGrasaRequestDto(

        @NotNull(message = "El sexo es obligatorio")
        SexoBiologico sexo,

        @NotNull(message = "La edad minima es obligatoria")
        @Min(value = 0, message = "La edad minima no puede ser negativa")
        Integer edadMin,

        @NotNull(message = "La edad maxima es obligatoria")
        @Max(value = 120, message = "La edad maxima no es realista")
        Integer edadMax,

        @NotNull(message = "El porcentaje minimo es obligatorio")
        @DecimalMin(value = "0.00", message = "El porcentaje minimo no puede ser negativo")
        BigDecimal porcentajeMin,

        @NotNull(message = "El porcentaje maximo es obligatorio")
        @DecimalMax(value = "100.00", message = "El porcentaje maximo no puede superar 100")
        BigDecimal porcentajeMax,

        @NotNull(message = "Los puntos de riesgo son obligatorios")
        @Min(value = 0, message = "Los puntos no pueden ser negativos")
        Integer puntosRiesgo,

        @NotNull(message = "La clasificacion es obligatoria")
        ClasificacionPorcentajeGrasa clasificacion
) {}