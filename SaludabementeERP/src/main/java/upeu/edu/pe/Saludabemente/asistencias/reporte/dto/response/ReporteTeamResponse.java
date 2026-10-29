package upeu.edu.pe.Saludabemente.asistencias.reporte.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ReporteTeamResponse(
        Long idTeam,
        String nombreTeam,
        String rangoConsultado,
        String modo,
        Integer totalMiembros,
        Integer totalActividades,
        BigDecimal porcentajeAsistencia,
        BandaResponse banda,
        Boolean al100,
        List<DiaTeamResponse> dias,
        Integer miembrosAl100,
        Integer miembrosEnRiesgo,
        List<MiembroRankingResponse> rankingMiembros,
        List<MiembroRankingResponse> topFaltantes,
        List<Long> miembrosConRachaFaltas
) {}