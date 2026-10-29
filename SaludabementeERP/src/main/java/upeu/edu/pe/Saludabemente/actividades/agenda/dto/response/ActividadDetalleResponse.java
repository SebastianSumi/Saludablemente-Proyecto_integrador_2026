package upeu.edu.pe.Saludabemente.actividades.agenda.dto.response;

import upeu.edu.pe.Saludabemente.actividades.imagen.dto.response.ImagenResponse;
import upeu.edu.pe.Saludabemente.actividades.inscripcion.dto.response.InscritoDetalleResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Detalle completo de una actividad: cabecera, creador, coordinador,
 * configuracion (materiales) e inscritos.
 */
public record ActividadDetalleResponse(
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
        String periodo,

        // ===== Creador =====
        Long idUsuarioCreador,
        String nombreCreador,
        String emailCreador,
        String rolCreador,
        LocalDateTime fechaCreacion,

        // ===== Coordinador =====
        Long idUsuarioCoordinador,
        String nombreCoordinador,

        // ===== Parametros operativos =====
        BigDecimal latitudOficial,
        BigDecimal longitudOficial,
        Integer toleranciaGeocercaMetros,
        Boolean requiereCheckInOut,
        Integer duracionMinutos,
        Boolean permiteAutoservicio,
        Boolean permiteEscaneoCoordinador,

        // ===== Configuracion =====
        List<String> materiales,

        // ===== Inscritos =====
        List<InscritoDetalleResponse> inscritos,
        Long totalInscritos,


        String logoUrl,
        List<ImagenResponse> imagenes
) {}
