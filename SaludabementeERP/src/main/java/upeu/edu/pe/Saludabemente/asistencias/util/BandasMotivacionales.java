package upeu.edu.pe.Saludabemente.asistencias.util;

import upeu.edu.pe.Saludabemente.asistencias.reporte.dto.response.BandaResponse;

import java.math.BigDecimal;

public final class BandasMotivacionales {

    private BandasMotivacionales() {}

    public static final BandaResponse CRITICO   = new BandaResponse("CRITICO",   "Necesita apoyo urgente",     "#DC2626", "🔴");
    public static final BandaResponse BAJO      = new BandaResponse("BAJO",      "Por debajo del promedio",    "#F97316", "🟠");
    public static final BandaResponse MEDIO     = new BandaResponse("MEDIO",     "En camino",                  "#FACC15", "🟡");
    public static final BandaResponse BUENO     = new BandaResponse("BUENO",     "Buen ritmo",                 "#22C55E", "🟢");
    public static final BandaResponse EXCELENTE = new BandaResponse("EXCELENTE", "Casi perfecto",              "#16A34A", "💚");
    public static final BandaResponse PERFECTO  = new BandaResponse("PERFECTO",  "¡Equipo perfecto!",          "#EAB308", "🏆");

    /**
     * Asigna banda según porcentaje (0-100).
     * Umbrales:
     *   [0,40)   → CRITICO
     *   [40,60)  → BAJO
     *   [60,75)  → MEDIO
     *   [75,90)  → BUENO
     *   [90,100) → EXCELENTE
     *   = 100    → PERFECTO
     */
    public static BandaResponse porPorcentaje(BigDecimal porcentaje) {
        if (porcentaje == null) return CRITICO;
        double p = porcentaje.doubleValue();
        if (p >= 100.0) return PERFECTO;
        if (p >= 90.0)  return EXCELENTE;
        if (p >= 75.0)  return BUENO;
        if (p >= 60.0)  return MEDIO;
        if (p >= 40.0)  return BAJO;
        return CRITICO;
    }
}