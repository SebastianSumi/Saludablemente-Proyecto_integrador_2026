package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto;

import jakarta.validation.constraints.*;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.DireccionRiesgo;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.clasificaciones.ClasificacionCarrera400m;

public record ReglaCarrera400mRequestDto(

        @NotNull(message = "El sexo es obligatorio")
        SexoBiologico sexo,

        @NotNull(message = "La edad minima es obligatoria")
        @Min(value = 0, message = "La edad minima no puede ser negativa")
        Integer edadMin,

        @NotNull(message = "La edad maxima es obligatoria")
        @Max(value = 120, message = "La edad maxima no es realista")
        Integer edadMax,

        @NotNull(message = "El tiempo minimo es obligatorio")
        @Min(value = 30, message = "El tiempo minimo es irreal")
        @Max(value = 600, message = "El tiempo minimo es irreal")
        Integer tiempoSegundosMin,

        @NotNull(message = "El tiempo maximo es obligatorio")
        @Min(value = 30, message = "El tiempo maximo es irreal")
        @Max(value = 900, message = "El tiempo maximo es irreal")
        Integer tiempoSegundosMax,

        @NotNull(message = "La direccion del riesgo es obligatoria")
        DireccionRiesgo direccionRiesgo,

        @NotNull(message = "Los puntos de riesgo son obligatorios")
        @Min(value = 0, message = "Los puntos no pueden ser negativos")
        Integer puntosRiesgo,

        @NotNull(message = "La clasificacion es obligatoria")
        ClasificacionCarrera400m clasificacion
) {}