package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.clasificaciones.ClasificacionPerimetroAbdominal;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReglaPerimetroAbdominalResponseDto(
        Long idReglaPerimetro,
        SexoBiologico sexo,
        Integer edadMin,
        Integer edadMax,
        BigDecimal perimetroCmMin,
        BigDecimal perimetroCmMax,
        Integer puntosRiesgo,
        ClasificacionPerimetroAbdominal clasificacion,
        EstadoRegla estado,
        LocalDateTime fechaBaja
) {}