package upeu.edu.pe.Saludabemente.nutricional.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Motor clínico puro. Todas las funciones son static, sin estado, sin BD.
 * Cubre las funciones 43-48 de PLANIFICACION.docx.
 *
 * NOTA CLÍNICA: las tablas de referencia (grasa, masa muscular) y los
 * umbrales de perímetro abdominal provienen del manual público de OMRON
 * y de criterios WHO/ATP III. Deben validarse con el nutricionista del
 * proyecto antes de pasar a producción. Cada tabla está marcada con
 * TODO: validar con nutricionista.
 */
public final class OmronRulesUtil {

    private OmronRulesUtil() {
        throw new UnsupportedOperationException("Clase utilitaria, no instanciable");
    }

    // ------------------------------------------------------------------
    // 1. IMC (funciones 43-44)
    // ------------------------------------------------------------------

    /**
     * IMC = peso(kg) / estatura(m)^2. Precisión Scale=1, HALF_UP.
     */
    public static BigDecimal calcularImc(BigDecimal pesoKg, BigDecimal estaturaCm) {
        if (pesoKg == null || estaturaCm == null) {
            return null;
        }
        if (estaturaCm.signum() <= 0) {
            throw new IllegalArgumentException("La estatura debe ser mayor a cero");
        }
        BigDecimal estaturaM = estaturaCm
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        return pesoKg
                .divide(estaturaM.multiply(estaturaM), 1, RoundingMode.HALF_UP);
    }

    /**
     * Clasificación WHO de IMC.
     * < 18.5  → DELGADEZ
     * 18.5-24.9 → NORMAL
     * 25.0-29.9 → SOBREPESO
     * >= 30.0 → OBESIDAD
     */
    public static String diagnosticarImc(BigDecimal imc) {
        if (imc == null) {
            return null;
        }
        double v = imc.doubleValue();
        if (v < 18.5) return "DELGADEZ";
        if (v < 25.0) return "NORMAL";
        if (v < 30.0) return "SOBREPESO";
        return "OBESIDAD";
    }

    // ------------------------------------------------------------------
    // 2. Perímetro abdominal (función 45)
    // ------------------------------------------------------------------

    /**
     * Riesgo cardiovascular por circunferencia de cintura.
     * Criterios WHO:
     *   Mujer: < 80 normal, 80-87 riesgo, >= 88 riesgo alto
     *   Hombre: < 94 normal, 94-101 riesgo, >= 102 riesgo alto
     * TODO: validar con nutricionista.
     */
    public static String diagnosticarPerimetroAbdominal(BigDecimal medidaCm, String sexo) {
        if (medidaCm == null || sexo == null) {
            return null;
        }
        double v = medidaCm.doubleValue();
        boolean mujer = "F".equalsIgnoreCase(sexo);

        if (mujer) {
            if (v < 80) return "NORMAL";
            if (v < 88) return "RIESGO";
            return "RIESGO_ALTO";
        } else {
            if (v < 94) return "NORMAL";
            if (v < 102) return "RIESGO";
            return "RIESGO_ALTO";
        }
    }

    // ------------------------------------------------------------------
    // 3. % Grasa corporal (función 46)
    // ------------------------------------------------------------------

    /**
     * Clasificación OMRON por sexo y rango etario.
     * Devuelve: BAJO, NORMAL, ALTO, MUY_ALTO.
     * TODO: validar con nutricionista.
     */
    public static String diagnosticarGrasa(BigDecimal porcentaje, String sexo, Integer edad) {
        if (porcentaje == null || sexo == null || edad == null) {
            return null;
        }
        double p = porcentaje.doubleValue();
        boolean mujer = "F".equalsIgnoreCase(sexo);

        if (mujer) {
            if (edad < 40) {
                if (p < 21) return "BAJO";
                if (p < 33) return "NORMAL";
                if (p < 39) return "ALTO";
                return "MUY_ALTO";
            } else if (edad < 60) {
                if (p < 23) return "BAJO";
                if (p < 34) return "NORMAL";
                if (p < 40) return "ALTO";
                return "MUY_ALTO";
            } else {
                if (p < 24) return "BAJO";
                if (p < 36) return "NORMAL";
                if (p < 42) return "ALTO";
                return "MUY_ALTO";
            }
        } else {
            if (edad < 40) {
                if (p < 8)  return "BAJO";
                if (p < 20) return "NORMAL";
                if (p < 25) return "ALTO";
                return "MUY_ALTO";
            } else if (edad < 60) {
                if (p < 11) return "BAJO";
                if (p < 22) return "NORMAL";
                if (p < 28) return "ALTO";
                return "MUY_ALTO";
            } else {
                if (p < 13) return "BAJO";
                if (p < 25) return "NORMAL";
                if (p < 30) return "ALTO";
                return "MUY_ALTO";
            }
        }
    }

    // ------------------------------------------------------------------
    // 4. % Masa muscular (función 47)
    // ------------------------------------------------------------------

    /**
     * Clasificación OMRON de masa muscular esquelética.
     * Devuelve: BAJO, NORMAL, ALTO.
     * TODO: validar con nutricionista.
     */
    public static String diagnosticarMasaMuscular(BigDecimal porcentaje, String sexo, Integer edad) {
        if (porcentaje == null || sexo == null || edad == null) {
            return null;
        }
        double p = porcentaje.doubleValue();
        boolean mujer = "F".equalsIgnoreCase(sexo);

        if (mujer) {
            if (edad < 40) {
                if (p < 24) return "BAJO";
                if (p < 31) return "NORMAL";
                return "ALTO";
            } else if (edad < 60) {
                if (p < 22) return "BAJO";
                if (p < 29) return "NORMAL";
                return "ALTO";
            } else {
                if (p < 20) return "BAJO";
                if (p < 27) return "NORMAL";
                return "ALTO";
            }
        } else {
            if (edad < 40) {
                if (p < 33) return "BAJO";
                if (p < 40) return "NORMAL";
                return "ALTO";
            } else if (edad < 60) {
                if (p < 31) return "BAJO";
                if (p < 38) return "NORMAL";
                return "ALTO";
            } else {
                if (p < 29) return "BAJO";
                if (p < 36) return "NORMAL";
                return "ALTO";
            }
        }
    }

    // ------------------------------------------------------------------
    // 5. Grasa visceral (función 48)
    // ------------------------------------------------------------------

    /**
     * Escala OMRON de grasa visceral (1-30).
     * <= 9  → NORMAL
     * 10-14 → ALTO
     * >= 15 → MUY_ALTO
     * TODO: validar con nutricionista.
     */
    public static String diagnosticarGrasaVisceral(BigDecimal nivel) {
        if (nivel == null) {
            return null;
        }
        double v = nivel.doubleValue();
        if (v <= 9)  return "NORMAL";
        if (v <= 14) return "ALTO";
        return "MUY_ALTO";
    }
}