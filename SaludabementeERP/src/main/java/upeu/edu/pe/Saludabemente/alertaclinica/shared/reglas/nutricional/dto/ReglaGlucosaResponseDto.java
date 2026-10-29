package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.clasificaciones.ClasificacionGlucosa;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReglaGlucosaResponseDto(
        Long idReglaGlucosa,
        SexoBiologico sexo,
        Integer edadMin,
        Integer edadMax,
        BigDecimal glucosaMgDlMin,
        BigDecimal glucosaMgDlMax,
        Integer puntosRiesgo,
        ClasificacionGlucosa clasificacion,
        EstadoRegla estado,
        LocalDateTime fechaBaja
) {}