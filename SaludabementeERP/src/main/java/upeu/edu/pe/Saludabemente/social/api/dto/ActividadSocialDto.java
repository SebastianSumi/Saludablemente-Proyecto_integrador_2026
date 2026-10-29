package upeu.edu.pe.Saludabemente.social.api.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Payload de sincronización definido en el Documento Maestro v1.0, sección 10.1. */
public record ActividadSocialDto(
        Long idActividad,
        String nombre,
        String descripcion,
        String lugar,
        LocalDate fechaActividad,
        LocalTime horaInicio,
        LocalTime horaFin,
        String estado,
        String logoUrl,
        List<String> imagenesUrls,
        String nombreCoordinador,
        Long idUsuarioCreador,
        Long idEquipoActividad
) {}
