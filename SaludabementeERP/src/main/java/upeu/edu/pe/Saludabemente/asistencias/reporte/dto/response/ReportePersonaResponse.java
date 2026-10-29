package upeu.edu.pe.Saludabemente.asistencias.reporte.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ReportePersonaResponse(
        Long idPersona,
        String nombreCompleto,
        Long idTeam,
        String nombreTeam,
        String rangoConsultado,
        BigDecimal porcentajeCumplimiento,
        BandaResponse banda,
        List<InsigniaResponse> insignias,
        Integer totalActividades,
        Integer totalAsistio,
        Integer totalAusente,
        Integer totalExonerado,
        Integer totalIncompleta,
        Integer rachaAsistenciasActual,
        Integer rachaAsistenciasMaxima,
        Integer rachaFaltasActual,
        Integer posicionEnTeam,
        Integer posicionAnterior,
        String movimiento,
        Boolean al100
) {}