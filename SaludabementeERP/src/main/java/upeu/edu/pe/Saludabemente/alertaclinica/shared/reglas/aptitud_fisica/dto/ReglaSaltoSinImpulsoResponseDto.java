package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.DireccionRiesgo;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.clasificaciones.ClasificacionPruebaFisica;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReglaSaltoSinImpulsoResponseDto(
        Long idReglaSalto,
        SexoBiologico sexo,
        Integer edadMin,
        Integer edadMax,
        BigDecimal distanciaCmMin,
        BigDecimal distanciaCmMax,
        DireccionRiesgo direccionRiesgo,
        Integer puntosRiesgo,
        ClasificacionPruebaFisica clasificacion,
        EstadoRegla estado,
        LocalDateTime fechaBaja
) {}