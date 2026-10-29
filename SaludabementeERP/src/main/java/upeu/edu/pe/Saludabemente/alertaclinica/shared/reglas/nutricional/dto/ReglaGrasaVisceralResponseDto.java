package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.clasificaciones.ClasificacionGrasaVisceral;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReglaGrasaVisceralResponseDto(
        Long idReglaVisceral,
        SexoBiologico sexo,
        Integer edadMin,
        Integer edadMax,
        BigDecimal nivelMin,
        BigDecimal nivelMax,
        Integer puntosRiesgo,
        ClasificacionGrasaVisceral clasificacion,
        EstadoRegla estado,
        LocalDateTime fechaBaja
) {}