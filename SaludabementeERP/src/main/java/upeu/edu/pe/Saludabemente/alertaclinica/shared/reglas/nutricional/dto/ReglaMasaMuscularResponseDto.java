package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.clasificaciones.ClasificacionMasaMuscular;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReglaMasaMuscularResponseDto(
        Long idReglaMusculo,
        SexoBiologico sexo,
        Integer edadMin,
        Integer edadMax,
        BigDecimal porcentajeMin,
        BigDecimal porcentajeMax,
        Integer puntosRiesgo,
        ClasificacionMasaMuscular clasificacion,
        EstadoRegla estado,
        LocalDateTime fechaBaja
) {}