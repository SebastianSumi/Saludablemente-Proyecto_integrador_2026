package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto;

import jakarta.validation.constraints.*;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.clasificaciones.ClasificacionPresionArterial;

public record ReglaPresionArterialRequestDto(

        @NotNull(message = "El sexo es obligatorio")
        SexoBiologico sexo,

        @NotNull(message = "La edad minima es obligatoria")
        @Min(value = 0, message = "La edad minima no puede ser negativa")
        Integer edadMin,

        @NotNull(message = "La edad maxima es obligatoria")
        @Max(value = 120, message = "La edad maxima no es realista")
        Integer edadMax,

        @NotNull(message = "La sistolica minima es obligatoria")
        @Min(value = 40, message = "La sistolica minima es irreal")
        @Max(value = 250, message = "La sistolica minima es irreal")
        Integer sistolicaMmHgMin,

        @NotNull(message = "La sistolica maxima es obligatoria")
        @Min(value = 40, message = "La sistolica maxima es irreal")
        @Max(value = 300, message = "La sistolica maxima es irreal")
        Integer sistolicaMmHgMax,

        @NotNull(message = "La diastolica minima es obligatoria")
        @Min(value = 20, message = "La diastolica minima es irreal")
        @Max(value = 150, message = "La diastolica minima es irreal")
        Integer diastolicaMmHgMin,

        @NotNull(message = "La diastolica maxima es obligatoria")
        @Min(value = 20, message = "La diastolica maxima es irreal")
        @Max(value = 200, message = "La diastolica maxima es irreal")
        Integer diastolicaMmHgMax,

        @NotNull(message = "Los puntos de riesgo son obligatorios")
        @Min(value = 0, message = "Los puntos no pueden ser negativos")
        Integer puntosRiesgo,

        @NotNull(message = "La clasificacion es obligatoria")
        ClasificacionPresionArterial clasificacion
) {}