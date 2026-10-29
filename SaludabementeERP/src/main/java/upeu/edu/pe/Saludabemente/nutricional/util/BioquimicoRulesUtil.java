package upeu.edu.pe.Saludabemente.nutricional.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Motor clínico puro para el detalle bioquímico.
 * Cubre las funciones 49-50 de PLANIFICACION.docx.
 *
 * NOTA CLÍNICA: los umbrales provienen de ATP III y criterios WHO.
 * TODO: validar con nutricionista.
 */
public final class BioquimicoRulesUtil {

    private BioquimicoRulesUtil() {
        throw new UnsupportedOperationException("Clase utilitaria, no instanciable");
    }

    // ------------------------------------------------------------------
    // 6. Perfil lipídico (función 49)
    // ------------------------------------------------------------------

    /**
     * Clasificación del perfil lipídico combinado.
     * Devuelve: NORMAL, LIMITROFE, ALTO.
     * Reglas:
     *   Colesterol total: <200 normal, 200-239 limítrofe, >=240 alto
     *   Triglicéridos:    <150 normal, 150-199 limítrofe, >=200 alto
     * Resultado final: el peor de los dos.
     */
    public static String evaluarPerfilLipidico(BigDecimal colesterol, BigDecimal trigliceridos) {
        if (colesterol == null && trigliceridos == null) {
            return null;
        }
        int nivelCol = clasificarColesterol(colesterol);
        int nivelTri = clasificarTrigliceridos(trigliceridos);
        int peor = Math.max(nivelCol, nivelTri);
        return switch (peor) {
            case 2 -> "ALTO";
            case 1 -> "LIMITROFE";
            default -> "NORMAL";
        };
    }

    private static int clasificarColesterol(BigDecimal v) {
        if (v == null) return -1;
        double d = v.doubleValue();
        if (d < 200) return 0;
        if (d < 240) return 1;
        return 2;
    }

    private static int clasificarTrigliceridos(BigDecimal v) {
        if (v == null) return -1;
        double d = v.doubleValue();
        if (d < 150) return 0;
        if (d < 200) return 1;
        return 2;
    }


    /**
     * Veredicto final bioquímico.
     * Devuelve:
     *   "NORMAL"                    si no hay marcadores alterados
     *   "ALTERADO: [GLUCOSA, COLESTEROL, ...]" si alguno lo está
     *
     * Reglas (ATP III / WHO):
     *   Glucosa ayunas:   <100 normal, 100-125 alterado, >=126 alterado severo
     *   Colesterol total: <200 normal, 200-239 alterado, >=240 alterado severo
     *   Triglicéridos:    <150 normal, 150-199 alterado, >=200 alterado severo
     *   Presión sistólica:   <120 normal, 120-139 elevada, >=140 alta
     *   Presión diastólica:  <80  normal, 80-89  elevada, >=90  alta
     */
    public static String calcularRiesgoGlobal(BigDecimal glucosa,
                                              BigDecimal colesterol,
                                              BigDecimal trigliceridos,
                                              Integer presionSistolica,
                                              Integer presionDiastolica) {
        List<String> alterados = new ArrayList<>();

        if (glucosa != null && glucosa.doubleValue() >= 100) {
            alterados.add("GLUCOSA");
        }
        if (colesterol != null && colesterol.doubleValue() >= 200) {
            alterados.add("COLESTEROL");
        }
        if (trigliceridos != null && trigliceridos.doubleValue() >= 150) {
            alterados.add("TRIGLICERIDOS");
        }
        if (presionSistolica != null && presionSistolica >= 130) {
            alterados.add("PRESION_SISTOLICA");
        }
        if (presionDiastolica != null && presionDiastolica >= 80) {
            alterados.add("PRESION_DIASTOLICA");
        }

        if (alterados.isEmpty()) {
            return "NORMAL";
        }
        return "ALTERADO: " + String.join(", ", alterados);
    }
}