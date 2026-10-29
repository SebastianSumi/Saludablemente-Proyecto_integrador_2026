package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.clasificaciones.ClasificacionColesterol;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReglaColesterolResponseDto(
        Long idReglaColesterol,
        SexoBiologico sexo,
        Integer edadMin,
        Integer edadMax,
        BigDecimal colesterolMgDlMin,
        BigDecimal colesterolMgDlMax,
        Integer puntosRiesgo,
        ClasificacionColesterol clasificacion,
        EstadoRegla estado,
        LocalDateTime fechaBaja
) {}