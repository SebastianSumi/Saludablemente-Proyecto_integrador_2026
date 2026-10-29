package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto;

import jakarta.validation.constraints.*;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.DireccionRiesgo;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.clasificaciones.ClasificacionPruebaFisica;

import java.math.BigDecimal;

public record ReglaSaltoSinImpulsoRequestDto(

        @NotNull(message = "El sexo es obligatorio")
        SexoBiologico sexo,

        @NotNull(message = "La edad minima es obligatoria")
        @Min(value = 0, message = "La edad minima no puede ser negativa")
        Integer edadMin,

        @NotNull(message = "La edad maxima es obligatoria")
        @Max(value = 120, message = "La edad maxima no es realista")
        Integer edadMax,

        @NotNull(message = "La distancia minima es obligatoria")
        @DecimalMin(value = "0.00", message = "La distancia minima no puede ser negativa")
        BigDecimal distanciaCmMin,

        @NotNull(message = "La distancia maxima es obligatoria")
        @DecimalMax(value = "400.00", message = "La distancia maxima es irreal")
        BigDecimal distanciaCmMax,

        @NotNull(message = "La direccion del riesgo es obligatoria")
        DireccionRiesgo direccionRiesgo,

        @NotNull(message = "Los puntos de riesgo son obligatorios")
        @Min(value = 0, message = "Los puntos no pueden ser negativos")
        Integer puntosRiesgo,

        @NotNull(message = "La clasificacion es obligatoria")
        ClasificacionPruebaFisica clasificacion
) {}