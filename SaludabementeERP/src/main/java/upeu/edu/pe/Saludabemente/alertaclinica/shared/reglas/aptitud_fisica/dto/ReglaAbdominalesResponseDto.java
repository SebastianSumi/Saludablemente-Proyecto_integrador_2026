package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.DireccionRiesgo;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.clasificaciones.ClasificacionPruebaFisica;

import java.time.LocalDateTime;

public record ReglaAbdominalesResponseDto(
        Long idReglaAbdominales,
        SexoBiologico sexo,
        Integer edadMin,
        Integer edadMax,
        Integer repeticionesMin,
        Integer repeticionesMax,
        DireccionRiesgo direccionRiesgo,
        Integer puntosRiesgo,
        ClasificacionPruebaFisica clasificacion,
        EstadoRegla estado,
        LocalDateTime fechaBaja
) {}