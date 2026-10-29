package upeu.edu.pe.Saludabemente.actividades.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Snapshot publico de una actividad para consumo de otros modulos.
 * Principal consumidor: Asistencias.
 */
public record ActividadSnapshotDto(
        Long idActividad,
        String nombre,
        String descripcion,
        String estado,
        LocalDate fechaActividad,
        LocalTime horaInicio,
        LocalTime horaFin,
        String lugar,
        BigDecimal latitudOficial,
        BigDecimal longitudOficial,
        Integer toleranciaGeocercaMetros,
        Boolean requiereCheckInOut,
        Integer duracionMinutos,
        String periodo,
        Long idUsuarioCreador,
        Boolean permiteAutoservicio,
        Boolean permiteEscaneoCoordinador
) {}