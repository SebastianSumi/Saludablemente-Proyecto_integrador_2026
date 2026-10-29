package upeu.edu.pe.Saludabemente.actividades.reporte.dto.response;

import java.util.List;

/**
 * Reporte D: Asistencia detallada de una actividad.
 * Cabecera = datos de la actividad.
 * Detalle  = lista de asistentes.
 *
 * PENDIENTE: requiere modulo Asistencias.
 */
public record ReporteAsistenciaResponse(
        // ===== Cabecera =====
        Long idActividad,
        String nombre,
        String fecha,
        String lugar,
        String estado,
        int totalAsistentes,

        // ===== Detalle =====
        List<AsistenteItem> asistentes
) {
    public record AsistenteItem(
            Long idPersona,
            String nombreCompleto,
            String horaMarcado,
            String metodoRegistro,
            Boolean sincronizado
    ) {}
}