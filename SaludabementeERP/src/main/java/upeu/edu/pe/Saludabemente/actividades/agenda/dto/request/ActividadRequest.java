package upeu.edu.pe.Saludabemente.actividades.agenda.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record ActividadRequest(
        // ===== Campos base =====
        @NotBlank @Size(max = 100) String nombre,
        @Size(max = 300) String descripcion,
        @NotNull LocalDate fechaActividad,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFin,
        @NotBlank @Size(max = 100) String lugar,
        @NotNull Long idUsuarioCreador,
        @NotNull Long idUsuarioCoordinador,

        // ===== Campos para Asistencias (opcionales) =====
        BigDecimal latitudOficial,
        BigDecimal longitudOficial,
        Integer toleranciaGeocercaMetros,
        Boolean requiereCheckInOut,
        Integer duracionMinutos,
        String periodo,
        Boolean permiteAutoservicio,
        Boolean permiteEscaneoCoordinador,
        Long idEquipoActividad
) {
    /** Compatibilidad con consumidores Java anteriores; JSON admite el campo opcional nuevo. */
    public ActividadRequest(String nombre,String descripcion,LocalDate fechaActividad,LocalTime horaInicio,LocalTime horaFin,String lugar,Long idUsuarioCreador,Long idUsuarioCoordinador,
            BigDecimal latitudOficial,BigDecimal longitudOficial,Integer toleranciaGeocercaMetros,Boolean requiereCheckInOut,Integer duracionMinutos,String periodo,Boolean permiteAutoservicio,Boolean permiteEscaneoCoordinador) {
        this(nombre,descripcion,fechaActividad,horaInicio,horaFin,lugar,idUsuarioCreador,idUsuarioCoordinador,latitudOficial,longitudOficial,toleranciaGeocercaMetros,requiereCheckInOut,duracionMinutos,periodo,permiteAutoservicio,permiteEscaneoCoordinador,null);
    }
}
