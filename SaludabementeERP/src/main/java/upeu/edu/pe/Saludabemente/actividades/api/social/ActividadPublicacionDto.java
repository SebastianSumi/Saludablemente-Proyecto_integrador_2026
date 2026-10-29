package upeu.edu.pe.Saludabemente.actividades.api.social;

import java.time.*;
import java.util.List;

/** Datos del dueño para ensamblar ActividadSocialDto; no depende del módulo consumidor. */
public record ActividadPublicacionDto(Long idActividad, String nombre, String descripcion,
        String lugar, LocalDate fechaActividad, LocalTime horaInicio, LocalTime horaFin,
        String estado, String logoUrl, List<String> imagenesUrls, String nombreCoordinador,
        Long idUsuarioCreador, Long idEquipoActividad) {
    public ActividadPublicacionDto { imagenesUrls = List.copyOf(imagenesUrls); }
}
