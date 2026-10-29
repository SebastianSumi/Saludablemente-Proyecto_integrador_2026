package upeu.edu.pe.Saludabemente.nutricional.api;

import java.math.BigDecimal;

public record MetricaNutricionalDto(
        TipoMetricaNutricional tipo,   // ← antes String
        BigDecimal valorMedido
) {}