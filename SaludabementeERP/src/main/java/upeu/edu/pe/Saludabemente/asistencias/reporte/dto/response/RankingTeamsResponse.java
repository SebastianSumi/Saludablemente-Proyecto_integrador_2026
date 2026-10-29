package upeu.edu.pe.Saludabemente.asistencias.reporte.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record RankingTeamsResponse(
        String rangoConsultado,
        String modo,
        BigDecimal porcentajeGeneral,
        BandaResponse bandaGeneral,
        Integer teamsAl100,
        Integer teamsEnRiesgo,
        Integer totalTeams,
        List<TeamRankingResponse> ranking,
        Map<String, Object> destacados
) {}