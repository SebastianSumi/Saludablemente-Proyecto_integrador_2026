package upeu.edu.pe.Saludabemente.aptitudfisica.util.metricas;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Implementación provisional del motor de métricas.
 * <p>
 * Los rangos por edad/sexo son estimaciones iniciales basadas en estándares
 * internacionales de condición física. <b>Pendiente de validación por los
 * evaluadores de la institución antes del despliegue productivo (DT-53a).</b>
 * <p>
 * Cuando los rangos definitivos estén validados, se crea
 * {@code MetricasAptitudV2} y se cambia la configuración de inyección sin
 * tocar servicios ni entidades.
 */
@Component
public class MetricasAptitudV1 implements MetricasAptitud {

    // ---- Niveles cualitativos ----
    private static final String EXCELENTE = "EXCELENTE";
    private static final String MUY_BUENO = "MUY_BUENO";
    private static final String BUENO     = "BUENO";
    private static final String PROMEDIO  = "PROMEDIO";
    private static final String BAJO      = "BAJO";

    // ---- Rangos etarios ----
    private enum Rango { JOVEN, ADULTO, MAYOR }

    private static Rango rangoDe(int edad) {
        if (edad <= 29) return Rango.JOVEN;
        if (edad <= 44) return Rango.ADULTO;
        return Rango.MAYOR;
    }

    // =========================================================
    // CORE — Abdominales 30s (mayor = mejor)
    // =========================================================
    @Override
    public String dxCore(int reps, String sexo, int edad) {
        Rango r = rangoDe(edad);
        boolean masculino = "M".equalsIgnoreCase(sexo);

        if (masculino) {
            return switch (r) {
                case JOVEN   -> nivelMayorMejor(reps, 15, 25, 35, 45);
                case ADULTO  -> nivelMayorMejor(reps, 12, 22, 30, 40);
                case MAYOR   -> nivelMayorMejor(reps, 10, 18, 25, 32);
            };
        } else {
            return switch (r) {
                case JOVEN   -> nivelMayorMejor(reps, 12, 20, 28, 36);
                case ADULTO  -> nivelMayorMejor(reps, 10, 18, 25, 32);
                case MAYOR   -> nivelMayorMejor(reps,  8, 14, 20, 26);
            };
        }
    }

    // =========================================================
    // TREN SUPERIOR — Planchas 30s (mayor = mejor)
    // =========================================================
    @Override
    public String dxTrenSuperior(int reps, String sexo, int edad) {
        Rango r = rangoDe(edad);
        boolean masculino = "M".equalsIgnoreCase(sexo);

        if (masculino) {
            return switch (r) {
                case JOVEN   -> nivelMayorMejor(reps, 10, 18, 26, 35);
                case ADULTO  -> nivelMayorMejor(reps,  8, 15, 22, 30);
                case MAYOR   -> nivelMayorMejor(reps,  6, 12, 18, 25);
            };
        } else {
            return switch (r) {
                case JOVEN   -> nivelMayorMejor(reps,  5, 10, 16, 22);
                case ADULTO  -> nivelMayorMejor(reps,  4,  8, 13, 18);
                case MAYOR   -> nivelMayorMejor(reps,  3,  6, 10, 14);
            };
        }
    }

    // =========================================================
    // TREN INFERIOR — Salto sin impulso (mayor = mejor)
    // =========================================================
    @Override
    public String dxTrenInferior(BigDecimal saltoCm, String sexo, int edad) {
        Rango r = rangoDe(edad);
        boolean masculino = "M".equalsIgnoreCase(sexo);
        double v = saltoCm.doubleValue();

        if (masculino) {
            return switch (r) {
                case JOVEN   -> nivelMayorMejorDecimal(v, 130, 155, 180, 200);
                case ADULTO  -> nivelMayorMejorDecimal(v, 120, 145, 165, 185);
                case MAYOR   -> nivelMayorMejorDecimal(v, 105, 125, 145, 165);
            };
        } else {
            return switch (r) {
                case JOVEN   -> nivelMayorMejorDecimal(v, 100, 120, 140, 160);
                case ADULTO  -> nivelMayorMejorDecimal(v,  90, 110, 130, 150);
                case MAYOR   -> nivelMayorMejorDecimal(v,  80,  95, 115, 135);
            };
        }
    }

    // =========================================================
    // CONSOLIDACIÓN DE FUERZA
    // =========================================================
    @Override
    public String consolidarFuerza(String dxCore, String dxSup, String dxInf) {
        int puntaje = puntajeDe(dxCore) + puntajeDe(dxSup) + puntajeDe(dxInf);
        // Rango total 3-15 → mapear a nivel
        if (puntaje >= 14) return EXCELENTE;
        if (puntaje >= 11) return MUY_BUENO;
        if (puntaje >= 8)  return BUENO;
        if (puntaje >= 5)  return PROMEDIO;
        return BAJO;
    }

    // =========================================================
    // CARDIOVASCULAR — Soga + 400m
    // =========================================================
    @Override
    public String dxCapacidadCardio(int soga30s, int carrera400mSeg, String sexo, int edad) {
        Rango r = rangoDe(edad);
        boolean masculino = "M".equalsIgnoreCase(sexo);

        // Soga: mayor = mejor
        String nivelSoga;
        if (masculino) {
            nivelSoga = switch (r) {
                case JOVEN   -> nivelMayorMejor(soga30s, 30, 50, 70, 90);
                case ADULTO  -> nivelMayorMejor(soga30s, 25, 45, 60, 80);
                case MAYOR   -> nivelMayorMejor(soga30s, 20, 35, 50, 65);
            };
        } else {
            nivelSoga = switch (r) {
                case JOVEN   -> nivelMayorMejor(soga30s, 25, 45, 60, 75);
                case ADULTO  -> nivelMayorMejor(soga30s, 20, 35, 50, 65);
                case MAYOR   -> nivelMayorMejor(soga30s, 15, 30, 40, 55);
            };
        }

        // Carrera 400m: menor = mejor
        String nivelCarrera;
        if (masculino) {
            nivelCarrera = switch (r) {
                case JOVEN   -> nivelMenorMejor(carrera400mSeg, 75, 90, 110, 130);
                case ADULTO  -> nivelMenorMejor(carrera400mSeg, 85, 100, 120, 145);
                case MAYOR   -> nivelMenorMejor(carrera400mSeg, 95, 115, 140, 165);
            };
        } else {
            nivelCarrera = switch (r) {
                case JOVEN   -> nivelMenorMejor(carrera400mSeg, 90, 105, 125, 150);
                case ADULTO  -> nivelMenorMejor(carrera400mSeg, 100, 120, 140, 165);
                case MAYOR   -> nivelMenorMejor(carrera400mSeg, 115, 135, 160, 185);
            };
        }

        int puntaje = puntajeDe(nivelSoga) + puntajeDe(nivelCarrera);
        if (puntaje >= 9) return EXCELENTE;
        if (puntaje >= 7) return MUY_BUENO;
        if (puntaje >= 5) return BUENO;
        if (puntaje >= 3) return PROMEDIO;
        return BAJO;
    }

    // =========================================================
    // FITNESS SCORE
    // =========================================================
    @Override
    public BigDecimal calcularFitnessScore(String dxCore, String dxSup, String dxInf, String dxCardio) {
        String fuerza = consolidarFuerza(dxCore, dxSup, dxInf);
        int pFuerza = puntajeDe(fuerza);
        int pCardio = puntajeDe(dxCardio);

        // Peso: fuerza 60%, cardio 40%
        double total = (pFuerza * 0.6 + pCardio * 0.4) * 20.0; // 0-100
        return BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP);
    }

    // =========================================================
    // HELPERS INTERNOS
    // =========================================================

    /** Mayor = mejor. Cortes: [bajo, promedio, bueno, muy bueno, excelente]. */
    private static String nivelMayorMejor(int v, int c1, int c2, int c3, int c4) {
        if (v >= c4) return EXCELENTE;
        if (v >= c3) return MUY_BUENO;
        if (v >= c2) return BUENO;
        if (v >= c1) return PROMEDIO;
        return BAJO;
    }

    /** Mayor = mejor, versión decimal. */
    private static String nivelMayorMejorDecimal(double v, double c1, double c2, double c3, double c4) {
        if (v >= c4) return EXCELENTE;
        if (v >= c3) return MUY_BUENO;
        if (v >= c2) return BUENO;
        if (v >= c1) return PROMEDIO;
        return BAJO;
    }

    /** Menor = mejor. Cortes: [excelente, muy bueno, bueno, promedio, bajo]. */
    private static String nivelMenorMejor(int v, int cEx, int cMb, int cB, int cP) {
        if (v <= cEx) return EXCELENTE;
        if (v <= cMb) return MUY_BUENO;
        if (v <= cB)  return BUENO;
        if (v <= cP)  return PROMEDIO;
        return BAJO;
    }

    /** Nivel → puntaje 1..5. */
    private static int puntajeDe(String nivel) {
        return switch (nivel) {
            case EXCELENTE -> 5;
            case MUY_BUENO -> 4;
            case BUENO     -> 3;
            case PROMEDIO  -> 2;
            default        -> 1;
        };
    }
}