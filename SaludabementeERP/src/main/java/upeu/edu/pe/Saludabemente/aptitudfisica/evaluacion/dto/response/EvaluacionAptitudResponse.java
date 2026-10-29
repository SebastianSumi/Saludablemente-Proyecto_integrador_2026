package upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.dto.response;

import upeu.edu.pe.Saludabemente.aptitudfisica.cardio.dto.response.CardioResponse;
import upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.dto.response.FuerzaResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EvaluacionAptitudResponse(
        Long id,
        Long idPersona,
        Long idPeriodo,
        String documentoIdentidadAlMomento,
        String nombresCompletosAlMomento,
        Integer edadAlMomento,
        String sexoAlMomento,
        String areaTrabajoAlMomento,
        LocalDate fechaEvaluacion,
        String estadoEvaluacion,
        String dictamenGlobal,
        String observacionesRestricciones,
        BigDecimal fitnessScore,
        FuerzaResponse detalleFuerza,
        CardioResponse detalleCardio
) {}