package upeu.edu.pe.Saludabemente.nutricional.api;

import java.math.BigDecimal;

/**
 * Detalle antropométrico con diagnósticos OMRON ya calculados.
 * Los campos llegan null si el indicador no fue medido.
 */
public record AntropometricoDto(
        BigDecimal pesoKg,
        BigDecimal estaturaCm,
        BigDecimal imc,
        String dxImc,
        BigDecimal perimetroAbdominalCm,
        String dxPerimetroAbdominal,
        BigDecimal porcentajeGrasa,
        String dxGrasa,
        BigDecimal porcentajeMasaMuscular,
        String dxMasaMuscular,
        BigDecimal porcentajeGrasaVisceral,
        String dxGrasaVisceral
) {}