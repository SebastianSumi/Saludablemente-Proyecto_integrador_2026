package upeu.edu.pe.Saludabemente.aptitudfisica.api;

import java.math.BigDecimal;

/**
 * Métrica individual dentro de un payload de evaluación física.
 * El {@code tipo} discrimina qué evaluador usar en el motor de alertas.
 */
public record MetricaFisicaDto(
        TipoMetrica tipo,
        BigDecimal valorMedido
) {}