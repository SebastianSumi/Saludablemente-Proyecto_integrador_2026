package upeu.edu.pe.Saludabemente.actividades.reporte.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * Reporte B: Agenda agrupada por periodo.
 * Cabecera = resumen del periodo.
 * Detalle  = lista de actividades del periodo.
 */
public record ReportePeriodoResponse(
        // ===== Cabecera =====
        String periodo,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        int totalActividades,
        int totalProgramadas,
        int totalEnCurso,
        int totalFinalizadas,
        int totalCanceladas,

        // ===== Detalle =====
        List<ActividadItem> actividades
) {
    public record ActividadItem(
            Long idActividad,
            String nombre,
            LocalDate fecha,
            String horaInicio,
            String horaFin,
            String lugar,
            String estado,
            Integer puntajeSalud,
            String nombreCoordinador
    ) {}
}