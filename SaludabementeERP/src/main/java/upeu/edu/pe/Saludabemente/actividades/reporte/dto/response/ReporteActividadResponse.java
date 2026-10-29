package upeu.edu.pe.Saludabemente.actividades.reporte.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Reporte A: Actividad con toda su configuracion.
 * Cabecera = datos de la actividad.
 * Detalle  = materiales requeridos.
 */
public record ReporteActividadResponse(
        // ===== Cabecera =====
        Long idActividad,
        String nombre,
        String descripcion,
        LocalDate fechaActividad,
        LocalTime horaInicio,
        LocalTime horaFin,
        String lugar,
        String estado,
        Integer puntajeSalud,
        String nombreCoordinador,

        // ===== Detalle =====
        List<String> materiales
) {}