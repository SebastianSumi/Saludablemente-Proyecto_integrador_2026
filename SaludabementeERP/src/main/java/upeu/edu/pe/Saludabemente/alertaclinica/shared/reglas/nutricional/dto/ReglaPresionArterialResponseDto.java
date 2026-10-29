package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.clasificaciones.ClasificacionPresionArterial;

import java.time.LocalDateTime;

public record ReglaPresionArterialResponseDto(
        Long idReglaPresion,
        SexoBiologico sexo,
        Integer edadMin,
        Integer edadMax,
        Integer sistolicaMmHgMin,
        Integer sistolicaMmHgMax,
        Integer diastolicaMmHgMin,
        Integer diastolicaMmHgMax,
        Integer puntosRiesgo,
        ClasificacionPresionArterial clasificacion,
        EstadoRegla estado,
        LocalDateTime fechaBaja
) {}