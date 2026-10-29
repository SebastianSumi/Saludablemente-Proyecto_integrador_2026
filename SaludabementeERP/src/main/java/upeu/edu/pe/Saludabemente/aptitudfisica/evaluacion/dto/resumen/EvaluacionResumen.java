package upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.dto.resumen;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EvaluacionResumen(
        Long id,
        String documentoIdentidadAlMomento,
        String nombresCompletosAlMomento,
        String areaTrabajoAlMomento,
        LocalDate fechaEvaluacion,
        String estadoEvaluacion,
        String dictamenGlobal,
        BigDecimal fitnessScore
) {}