package upeu.edu.pe.Saludabemente.nutricional.api;

import java.time.LocalDate;

/**
 * Cabecera de evaluación nutricional expuesta a Recomendaciones IA.
 */
public record EvaluacionNutricionalDto(
        Long idEvaluacion,
        Long idPersona,
        LocalDate fechaEvaluacion,
        String periodoSemestral,
        String observaciones
) {}