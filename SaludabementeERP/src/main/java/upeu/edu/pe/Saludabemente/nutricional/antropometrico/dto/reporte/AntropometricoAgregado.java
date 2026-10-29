package upeu.edu.pe.Saludabemente.nutricional.antropometrico.dto.reporte;

/**
 * Distribución de diagnósticos IMC por periodo.
 * Alimenta el reporte del subdominio antropométrico.
 */
public record AntropometricoAgregado(
        String dxImc,
        long cantidad
) {}