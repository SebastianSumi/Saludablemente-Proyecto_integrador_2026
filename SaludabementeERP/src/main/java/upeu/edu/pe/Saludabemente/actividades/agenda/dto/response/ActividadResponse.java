package upeu.edu.pe.Saludabemente.actividades.agenda.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record ActividadResponse(
        // ===== Campos base =====
        Long idActividad,
        String nombre,
        String descripcion,
        LocalDate fechaActividad,
        LocalTime horaInicio,
        LocalTime horaFin,
        String lugar,
        String estado,
        Long idUsuarioCreador,
        Long idUsuarioCoordinador,
        String nombreCoordinador,

        // ===== Campos para Asistencias =====
        BigDecimal latitudOficial,
        BigDecimal longitudOficial,
        Integer toleranciaGeocercaMetros,
        Boolean requiereCheckInOut,
        Integer duracionMinutos,
        String periodo,
        Boolean permiteAutoservicio,
        Boolean permiteEscaneoCoordinador,
        String logoUrl
) {}