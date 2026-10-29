package upeu.edu.pe.Saludabemente.actividades.reporte.dto.request;

import java.time.LocalDate;

/**
 * Filtro opcional para reportes de periodo.
 * Si no se envia, se usa el mes actual.
 */
public record ReporteFiltroRequest(
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String periodo  // Ej. "2026-I". Alternativa a fechaInicio/fechaFin.
) {}