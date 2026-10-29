package upeu.edu.pe.Saludabemente.asistencias.api.dto;

import java.math.BigDecimal;

public record ParticipacionActividadDTO(
        Long idActividad,
        Integer totalEsperados,
        Integer totalAsistentes,
        Integer totalAusentes,
        Integer totalExonerados,
        Integer totalCuarentenaPendiente,
        BigDecimal porcentajeParticipacion,
        String metodoMasUsado
) {}