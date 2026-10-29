package upeu.edu.pe.Saludabemente.nutricional.api;

/**
 * Evaluación histórica completa: cabecera + antropometría + bioquímica.
 * La bioquímica puede ser null si esa evaluación no tuvo laboratorio.
 */
public record EvaluacionHistoricaDto(
        EvaluacionNutricionalDto cabecera,
        AntropometricoDto antropometria,
        BioquimicoDto bioquimica
) {}