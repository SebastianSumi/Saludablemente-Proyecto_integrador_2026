package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.DireccionRiesgo;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.clasificaciones.ClasificacionCarrera400m;

import java.time.LocalDateTime;

public record ReglaCarrera400mResponseDto(
        Long idReglaCarrera,
        SexoBiologico sexo,
        Integer edadMin,
        Integer edadMax,
        Integer tiempoSegundosMin,
        Integer tiempoSegundosMax,
        DireccionRiesgo direccionRiesgo,
        Integer puntosRiesgo,
        ClasificacionCarrera400m clasificacion,
        EstadoRegla estado,
        LocalDateTime fechaBaja
) {}