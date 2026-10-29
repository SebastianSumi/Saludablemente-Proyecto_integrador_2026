package upeu.edu.pe.Saludabemente.nutricional.bioquimico.dto.reporte;

/**
 * Distribución de riesgo global bioquímico por periodo.
 * Alimenta el reporte del subdominio bioquímico y el módulo de auditoría de salud.
 */
public record BioquimicoAgregado(
        String dxBioquimico,
        long cantidad
) {}