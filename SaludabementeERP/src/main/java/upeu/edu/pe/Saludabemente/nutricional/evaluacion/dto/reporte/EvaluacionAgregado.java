package upeu.edu.pe.Saludabemente.nutricional.evaluacion.dto.reporte;

/**
 * Métricas agregadas del dashboard.
 * Alimenta GET /agregados.
 * Los valores numéricos llegan de JPQL (COUNT, AVG).
 */
public record EvaluacionAgregado(
        long total,
        Double promedioImc,
        Double promedioPorcentajeGrasa,
        Double promedioPorcentajeMasaMuscular
) {}