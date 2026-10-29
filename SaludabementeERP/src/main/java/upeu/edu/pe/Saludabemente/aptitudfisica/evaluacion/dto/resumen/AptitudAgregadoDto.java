package upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.dto.resumen;

public record AptitudAgregadoDto(
        long total,
        long pendientes,
        long completas,
        long anuladas,
        long aptos,
        long aptosConRestricciones,
        long noAptos
) {}