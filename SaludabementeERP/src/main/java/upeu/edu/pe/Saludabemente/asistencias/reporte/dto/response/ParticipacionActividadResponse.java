package upeu.edu.pe.Saludabemente.asistencias.reporte.dto.response;

import java.math.BigDecimal;

public record ParticipacionActividadResponse(
        Long idActividad,
        Integer totalEsperados,
        Integer totalAsistentes,
        Integer totalAusentes,
        Integer totalExonerados,
        Integer totalIncompletas,
        Integer totalCuarentenaPendiente,
        BigDecimal porcentajeParticipacion,
        BandaResponse banda,
        String metodoMasUsado
) {}