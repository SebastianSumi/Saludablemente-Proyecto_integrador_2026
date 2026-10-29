package upeu.edu.pe.Saludabemente.actividades.reporte.dto.response;

import java.util.List;

/**
 * Reporte C: Participacion agregada por equipo.
 * Cabecera = datos del equipo.
 * Detalle  = actividades del equipo + cantidad de asistentes.
 *
 * PENDIENTE: requiere modulo Asistencias.
 */
public record ReporteParticipacionResponse(
        // ===== Cabecera =====
        Long idEquipo,
        String nombreEquipo,
        int totalActividades,
        int totalAsistentesUnicos,

        // ===== Detalle =====
        List<ActividadParticipacionItem> actividades
) {
    public record ActividadParticipacionItem(
            Long idActividad,
            String nombre,
            String fecha,
            int cantidadAsistentes,
            double porcentajeAsistencia
    ) {}
}