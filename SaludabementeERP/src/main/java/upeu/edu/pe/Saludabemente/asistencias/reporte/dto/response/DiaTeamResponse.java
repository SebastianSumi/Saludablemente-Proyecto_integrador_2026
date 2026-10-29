package upeu.edu.pe.Saludabemente.asistencias.reporte.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DiaTeamResponse(
        LocalDate fecha,
        BigDecimal porcentaje,
        BandaResponse banda,
        Integer asistieron,
        Integer total,
        List<Long> faltantes,
        Boolean al100
) {}