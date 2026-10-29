package upeu.edu.pe.Saludabemente.asistencias.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class GeocercaUtil {

    private GeocercaUtil() {}

    private static final double RADIO_TIERRA_METROS = 6_371_000.0;
    private static final double MULTIPLICADOR_SOSPECHOSO = 2.0;

    /**
     * Fórmula de Haversine.
     * Devuelve la distancia en metros entre dos puntos (lat/lon en grados decimales).
     * Si algún valor es null, devuelve null.
     * Redondea a 2 decimales para almacenar.
     */
    public static BigDecimal calcularDistanciaMetros(
            BigDecimal lat1, BigDecimal lon1,
            BigDecimal lat2, BigDecimal lon2) {

        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return null;
        }

        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distancia = RADIO_TIERRA_METROS * c;

        return BigDecimal.valueOf(distancia).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Clasifica la geocerca según la distancia y la tolerancia.
     * Si la distancia es null, devuelve SIN_COORDENADAS.
     * Si la tolerancia es null o <= 0, usa el default de ConstantesAsistencia.
     */
    public static ClasificacionGeocerca clasificar(BigDecimal distancia, Integer toleranciaMetros) {

        if (distancia == null) {
            return ClasificacionGeocerca.SIN_COORDENADAS;
        }

        int tolerancia = (toleranciaMetros != null && toleranciaMetros > 0)
                ? toleranciaMetros
                : ConstantesAsistencia.TOLERANCIA_GEOCERCA_DEFAULT_METROS;

        BigDecimal tol = BigDecimal.valueOf(tolerancia);
        BigDecimal tolExtendida = tol.multiply(BigDecimal.valueOf(MULTIPLICADOR_SOSPECHOSO));

        if (distancia.compareTo(tol) <= 0) {
            return ClasificacionGeocerca.OK;
        }
        if (distancia.compareTo(tolExtendida) <= 0) {
            return ClasificacionGeocerca.SOSPECHOSO;
        }
        return ClasificacionGeocerca.FUERA;
    }

    /**
     * Atajo: calcula y clasifica en un solo paso.
     */
    public static ResultadoGeocerca evaluar(
            BigDecimal latEscaneo, BigDecimal lonEscaneo,
            BigDecimal latOficial, BigDecimal lonOficial,
            Integer toleranciaMetros) {

        BigDecimal distancia = calcularDistanciaMetros(
                latEscaneo, lonEscaneo, latOficial, lonOficial);

        ClasificacionGeocerca clasificacion = clasificar(distancia, toleranciaMetros);

        return new ResultadoGeocerca(distancia, clasificacion);
    }

    public record ResultadoGeocerca(
            BigDecimal distanciaMetros,
            ClasificacionGeocerca clasificacion
    ) {}
}