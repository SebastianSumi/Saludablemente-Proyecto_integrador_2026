package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto;

import jakarta.validation.constraints.*;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.clasificaciones.ClasificacionTrigliceridos;

import java.math.BigDecimal;

public record ReglaTrigliceridosRequestDto(

        @NotNull(message = "El sexo es obligatorio")
        SexoBiologico sexo,

        @NotNull(message = "La edad minima es obligatoria")
        @Min(value = 0, message = "La edad minima no puede ser negativa")
        Integer edadMin,

        @NotNull(message = "La edad maxima es obligatoria")
        @Max(value = 120, message = "La edad maxima no es realista")
        Integer edadMax,

        @NotNull(message = "Los trigliceridos minimos son obligatorios")
        @DecimalMin(value = "0.00", message = "Los trigliceridos minimos no pueden ser negativos")
        BigDecimal trigliceridosMgDlMin,

        @NotNull(message = "Los trigliceridos maximos son obligatorios")
        @DecimalMax(value = "2000.00", message = "Los trigliceridos maximos son irreales")
        BigDecimal trigliceridosMgDlMax,

        @NotNull(message = "Los puntos de riesgo son obligatorios")
        @Min(value = 0, message = "Los puntos no pueden ser negativos")
        Integer puntosRiesgo,

        @NotNull(message = "La clasificacion es obligatoria")
        ClasificacionTrigliceridos clasificacion
) {}