package upeu.edu.pe.Saludabemente.asistencias.util;

import upeu.edu.pe.Saludabemente.asistencias.enums.EstadoAsistencia;
import upeu.edu.pe.Saludabemente.asistencias.registro.entity.Asistencia;

import java.util.Comparator;
import java.util.List;

public final class RachaCalculator {

    private RachaCalculator() {}

    /**
     * Calcula la racha actual de faltas injustificadas consecutivas.
     * Reglas:
     *  - ASISTIO, EXONERADO, INCOMPLETA → rompen la racha (retorna 0 si es el más reciente).
     *  - AUSENTE → suma 1.
     * Se recorre desde la más reciente hacia atrás.
     */
    public static int calcularRachaFaltas(List<Asistencia> asistencias) {
        if (asistencias == null || asistencias.isEmpty()) {
            return 0;
        }
        List<Asistencia> ordenadas = asistencias.stream()
                .sorted(Comparator.comparing(Asistencia::getHoraMarcado).reversed())
                .toList();

        int racha = 0;
        for (Asistencia a : ordenadas) {
            if (a.getEstado() == EstadoAsistencia.AUSENTE) {
                racha++;
            } else {
                break;
            }
        }
        return racha;
    }

    /**
     * Racha positiva de asistencias consecutivas más recientes.
     */
    public static int calcularRachaAsistencias(List<Asistencia> asistencias) {
        if (asistencias == null || asistencias.isEmpty()) {
            return 0;
        }
        List<Asistencia> ordenadas = asistencias.stream()
                .sorted(Comparator.comparing(Asistencia::getHoraMarcado).reversed())
                .toList();

        int racha = 0;
        for (Asistencia a : ordenadas) {
            if (a.getEstado() == EstadoAsistencia.ASISTIO) {
                racha++;
            } else {
                break;
            }
        }
        return racha;
    }
}