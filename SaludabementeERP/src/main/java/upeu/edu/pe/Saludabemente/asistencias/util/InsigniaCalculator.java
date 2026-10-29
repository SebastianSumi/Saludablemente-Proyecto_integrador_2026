package upeu.edu.pe.Saludabemente.asistencias.util;

import upeu.edu.pe.Saludabemente.asistencias.enums.EstadoAsistencia;
import upeu.edu.pe.Saludabemente.asistencias.registro.entity.Asistencia;
import upeu.edu.pe.Saludabemente.asistencias.reporte.dto.response.InsigniaResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class InsigniaCalculator {

    private InsigniaCalculator() {}

    private static final InsigniaResponse PERFECTO = new InsigniaResponse(
            "PERFECTO", "🏆", "Equipo perfecto", "Asistió a todas las actividades del periodo");
    private static final InsigniaResponse CONSTANTE = new InsigniaResponse(
            "CONSTANTE", "🎯", "Constante", "≥ 90% sin rachas de faltas");
    private static final InsigniaResponse MEJORANDO = new InsigniaResponse(
            "MEJORANDO", "📈", "En mejora", "Últimas 3 actividades asistidas");
    private static final InsigniaResponse RACHA_POSITIVA = new InsigniaResponse(
            "RACHA_POSITIVA", "🔥", "Racha positiva", "5 o más asistencias consecutivas");
    private static final InsigniaResponse SIN_FALTAS = new InsigniaResponse(
            "SIN_FALTAS", "✨", "Sin faltas", "0 ausencias en el periodo");
    private static final InsigniaResponse MAS_ASISTIO = new InsigniaResponse(
            "MAS_ASISTIO", "🥇", "Más asistió", "Top 1 del team");
    private static final InsigniaResponse MAS_FALTO = new InsigniaResponse(
            "MAS_FALTO", "⚠️", "Necesita apoyo", "Top 1 en faltas del team");

    /**
     * Calcula insignias de una persona en un periodo.
     */
    public static List<InsigniaResponse> calcular(
            List<Asistencia> historial,
            BigDecimal porcentaje,
            boolean esTop1Team,
            boolean esTopFaltasTeam) {

        List<InsigniaResponse> insignias = new ArrayList<>();
        if (historial == null || historial.isEmpty()) return insignias;

        long total = historial.size();
        long asistio = historial.stream()
                .filter(a -> a.getEstado() == EstadoAsistencia.ASISTIO).count();
        long ausente = historial.stream()
                .filter(a -> a.getEstado() == EstadoAsistencia.AUSENTE).count();

        int rachaPositiva = RachaCalculator.calcularRachaAsistencias(historial);
        int rachaFaltas = RachaCalculator.calcularRachaFaltas(historial);

        // PERFECTO: 100%
        if (porcentaje != null && porcentaje.compareTo(BigDecimal.valueOf(100)) >= 0) {
            insignias.add(PERFECTO);
        }

        // SIN_FALTAS
        if (ausente == 0) {
            insignias.add(SIN_FALTAS);
        }

        // CONSTANTE: ≥ 90% sin racha de faltas
        if (porcentaje != null && porcentaje.compareTo(BigDecimal.valueOf(90)) >= 0
                && rachaFaltas == 0 && !insignias.contains(PERFECTO)) {
            insignias.add(CONSTANTE);
        }

        // RACHA_POSITIVA: ≥ 5
        if (rachaPositiva >= 5) {
            insignias.add(RACHA_POSITIVA);
        }

        // MEJORANDO: últimas 3 asistidas
        if (historial.size() >= 3) {
            List<Asistencia> ultimas3 = historial.stream()
                    .sorted(Comparator.comparing(Asistencia::getHoraMarcado).reversed())
                    .limit(3)
                    .toList();
            boolean todas = ultimas3.stream()
                    .allMatch(a -> a.getEstado() == EstadoAsistencia.ASISTIO);
            if (todas) insignias.add(MEJORANDO);
        }

        // MAS_ASISTIO / MAS_FALTO
        if (esTop1Team) insignias.add(MAS_ASISTIO);
        if (esTopFaltasTeam) insignias.add(MAS_FALTO);

        return insignias;
    }
}