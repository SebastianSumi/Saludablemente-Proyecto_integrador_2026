package upeu.edu.pe.Saludabemente.asistencias.reporte.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record TeamRankingResponse(
        Integer posicion,
        Long idTeam,
        String nombreTeam,
        BigDecimal porcentajeAsistencia,
        BandaResponse banda,
        List<InsigniaResponse> insignias,
        String movimiento,
        Integer posicionAnterior,
        Integer totalMiembros,
        Integer totalFaltas,
        Integer diasAl100,
        BigDecimal porcentajeDiasAl100
) {}