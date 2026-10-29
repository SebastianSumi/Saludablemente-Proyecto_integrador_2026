package upeu.edu.pe.Saludabemente.asistencias.util;

import java.time.Instant;

public final class QrActividadUtil {

    private QrActividadUtil() {}

    private static final String PREFIJO = "UPeU-ASIS-A:";

    /**
     * Construye el código QR general.
     * Formato: UPeU-ASIS-A:<idActividad>:<version>:<timestampMillis>
     */
    public static String construir(Long idActividad, Integer version, Long timestampMillis) {
        return PREFIJO + idActividad + ":" + version + ":" + timestampMillis;
    }

    /**
     * Parsea un código QR general.
     * Devuelve null si el formato es inválido.
     */
    public static ParseResult parsear(String codigoQr) {
        if (codigoQr == null || !codigoQr.startsWith(PREFIJO)) {
            return null;
        }
        String[] partes = codigoQr.split(":");
        if (partes.length != 4) {
            return null;
        }
        try {
            Long idActividad = Long.parseLong(partes[1]);
            Integer version = Integer.parseInt(partes[2]);
            Long timestamp = Long.parseLong(partes[3]);
            return new ParseResult(idActividad, version, timestamp);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Verifica si un timestamp está dentro de la tolerancia de reloj.
     */
    public static boolean dentroDeToleranciaReloj(Long timestampMillis, int toleranciaSegundos) {
        long ahora = Instant.now().toEpochMilli();
        long diffSegundos = Math.abs(ahora - timestampMillis) / 1000L;
        return diffSegundos <= toleranciaSegundos;
    }

    public record ParseResult(Long idActividad, Integer version, Long timestampMillis) {}
}