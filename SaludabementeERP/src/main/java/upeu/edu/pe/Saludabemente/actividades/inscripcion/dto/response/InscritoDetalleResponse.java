package upeu.edu.pe.Saludabemente.actividades.inscripcion.dto.response;

import java.time.LocalDateTime;

/**
 * Detalle de una persona inscrita a una actividad.
 * Combina la fila de {@code ACTIVIDAD_INSCRITO} con el snapshot de Persona.
 */
public record InscritoDetalleResponse(
        Long idPersona,
        String nombreCompleto,
        String nombres,
        String apellidoPaterno,
        String apellidoMaterno,
        String areaTrabajo,
        String celular,
        String sexo,
        Boolean activo,
        Boolean inscritoPorTodos,
        LocalDateTime fechaInscripcion
) {}
