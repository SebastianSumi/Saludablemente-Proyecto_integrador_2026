package upeu.edu.pe.Saludabemente.aptitudfisica.api;

import java.time.LocalDate;
import java.util.List;

/**
 * Payload que Aptitud Física envía al motor de alertas al completar
 * una evaluación. Contiene solo las métricas con valor real —
 * las pruebas no realizadas se omiten.
 */
public record EvaluacionFisicaIngresoDto(
        Long idEvaluacionFisica,
        Long idPersona,
        LocalDate fechaEvaluacion,
        List<MetricaFisicaDto> metricas
) {}