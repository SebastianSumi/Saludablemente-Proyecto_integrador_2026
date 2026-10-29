package upeu.edu.pe.Saludabemente.nutricional.api;

import java.time.LocalDate;
import java.util.List;

public record EvaluacionNutricionalIngresoDto(
        Long idEvaluacionNutricional,
        Long idPersona,
        LocalDate fechaEvaluacion,
        List<MetricaNutricionalDto> metricas
) {}