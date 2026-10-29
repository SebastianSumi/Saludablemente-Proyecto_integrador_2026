package upeu.edu.pe.Saludabemente.asistencias.util;

import java.time.LocalDate;

public final class PeriodoUtil {

    private PeriodoUtil() {}

    public record Rango(LocalDate desde, LocalDate hasta, String etiqueta) {}

    /**
     * Convierte "2026-I" → { 2026-01-01, 2026-06-30, "2026-I" }
     * Convierte "2026-II" → { 2026-07-01, 2026-12-31, "2026-II" }
     * Devuelve null si el formato es inválido.
     */
    public static Rango parsear(String periodo) {
        if (periodo == null || !periodo.matches("\\d{4}-(I|II)")) {
            return null;
        }
        String[] partes = periodo.split("-");
        int anio = Integer.parseInt(partes[0]);
        if ("I".equals(partes[1])) {
            return new Rango(
                    LocalDate.of(anio, 1, 1),
                    LocalDate.of(anio, 6, 30),
                    periodo);
        }
        return new Rango(
                LocalDate.of(anio, 7, 1),
                LocalDate.of(anio, 12, 31),
                periodo);
    }

    /**
     * Construye un Rango a partir de fechas explícitas.
     */
    public static Rango porFechas(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) return null;
        return new Rango(desde, hasta, desde + "/" + hasta);
    }
}