package upeu.edu.pe.Saludabemente.asistencias.api.dto;

import java.math.BigDecimal;

public record HistorialAsistenciaDTO(
        Long idPersona,
        String periodo,
        Integer totalActividades,
        Integer totalAsistio,
        Integer totalAusente,
        Integer totalExonerado,
        Integer totalIncompleta,
        BigDecimal porcentajeCumplimiento
) {}