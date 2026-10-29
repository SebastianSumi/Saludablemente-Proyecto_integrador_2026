package upeu.edu.pe.Saludabemente.asistencias.reporte.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record MiembroRankingResponse(
        Long idPersona,
        String nombreCompleto,
        BigDecimal porcentajeCumplimiento,
        BandaResponse banda,
        List<InsigniaResponse> insignias,
        Integer posicionActual,
        Integer posicionAnterior,
        String movimiento,
        Integer totalAsistio,
        Integer totalAusente,
        Integer rachaAsistenciasActual,
        Integer rachaFaltasActual,
        Boolean al100
) {}