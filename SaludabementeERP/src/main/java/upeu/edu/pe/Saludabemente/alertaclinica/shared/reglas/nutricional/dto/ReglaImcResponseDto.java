package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.clasificaciones.ClasificacionImc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReglaImcResponseDto(
        Long idReglaImc,
        SexoBiologico sexo,
        Integer edadMin,
        Integer edadMax,
        BigDecimal imcMin,
        BigDecimal imcMax,
        Integer puntosRiesgo,
        ClasificacionImc clasificacion,
        EstadoRegla estado,
        LocalDateTime fechaBaja
) {}