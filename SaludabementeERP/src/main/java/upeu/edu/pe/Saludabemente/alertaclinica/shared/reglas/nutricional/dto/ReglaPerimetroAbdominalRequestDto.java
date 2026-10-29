package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto;

import jakarta.validation.constraints.*;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.clasificaciones.ClasificacionPerimetroAbdominal;

import java.math.BigDecimal;

public record ReglaPerimetroAbdominalRequestDto(

        @NotNull(message = "El sexo es obligatorio")
        SexoBiologico sexo,

        @NotNull(message = "La edad minima es obligatoria")
        @Min(value = 0, message = "La edad minima no puede ser negativa")
        Integer edadMin,

        @NotNull(message = "La edad maxima es obligatoria")
        @Max(value = 120, message = "La edad maxima no es realista")
        Integer edadMax,

        @NotNull(message = "El perimetro minimo es obligatorio")
        @DecimalMin(value = "0.00", message = "El perimetro minimo no puede ser negativo")
        BigDecimal perimetroCmMin,

        @NotNull(message = "El perimetro maximo es obligatorio")
        @DecimalMax(value = "300.00", message = "El perimetro maximo es irreal")
        BigDecimal perimetroCmMax,

        @NotNull(message = "Los puntos de riesgo son obligatorios")
        @Min(value = 0, message = "Los puntos no pueden ser negativos")
        Integer puntosRiesgo,

        @NotNull(message = "La clasificacion es obligatoria")
        ClasificacionPerimetroAbdominal clasificacion
) {}