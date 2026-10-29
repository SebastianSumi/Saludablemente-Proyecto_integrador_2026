package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.clasificaciones.ClasificacionTrigliceridos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReglaTrigliceridosResponseDto(
        Long idReglaTrigliceridos,
        SexoBiologico sexo,
        Integer edadMin,
        Integer edadMax,
        BigDecimal trigliceridosMgDlMin,
        BigDecimal trigliceridosMgDlMax,
        Integer puntosRiesgo,
        ClasificacionTrigliceridos clasificacion,
        EstadoRegla estado,
        LocalDateTime fechaBaja
) {}