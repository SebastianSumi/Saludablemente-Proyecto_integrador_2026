package upeu.edu.pe.Saludabemente.asistencias.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class GeocercaUtilTest {

    // Coordenadas de referencia (UPeU Lima, aprox.)
    private static final BigDecimal LAT_UPEU = new BigDecimal("-12.0464000");
    private static final BigDecimal LON_UPEU = new BigDecimal("-77.0428000");

    @Test
    void distancia_mismoPunto_esCero() {
        BigDecimal d = GeocercaUtil.calcularDistanciaMetros(
                LAT_UPEU, LON_UPEU, LAT_UPEU, LON_UPEU);
        assertThat(d).isNotNull();
        assertThat(d.doubleValue()).isLessThan(1.0);
    }

    @Test
    void distancia_puntosConocidos_aprox100m() {
        // ~100 m al norte (0.0009° lat ≈ 100 m)
        BigDecimal lat2 = LAT_UPEU.add(new BigDecimal("0.0009"));
        BigDecimal d = GeocercaUtil.calcularDistanciaMetros(
                LAT_UPEU, LON_UPEU, lat2, LON_UPEU);
        assertThat(d.doubleValue()).isBetween(95.0, 105.0);
    }

    @Test
    void distancia_puntosConocidos_aprox1km() {
        // ~1 km al norte (0.009° lat ≈ 1 km)
        BigDecimal lat2 = LAT_UPEU.add(new BigDecimal("0.009"));
        BigDecimal d = GeocercaUtil.calcularDistanciaMetros(
                LAT_UPEU, LON_UPEU, lat2, LON_UPEU);
        assertThat(d.doubleValue()).isBetween(990.0, 1010.0);
    }

    @Test
    void distancia_conNull_devuelveNull() {
        assertThat(GeocercaUtil.calcularDistanciaMetros(
                null, LON_UPEU, LAT_UPEU, LON_UPEU)).isNull();
        assertThat(GeocercaUtil.calcularDistanciaMetros(
                LAT_UPEU, null, LAT_UPEU, LON_UPEU)).isNull();
        assertThat(GeocercaUtil.calcularDistanciaMetros(
                LAT_UPEU, LON_UPEU, null, LON_UPEU)).isNull();
        assertThat(GeocercaUtil.calcularDistanciaMetros(
                LAT_UPEU, LON_UPEU, LAT_UPEU, null)).isNull();
    }

    @Test
    void clasificar_dentroDeTolerancia_OK() {
        ClasificacionGeocerca c = GeocercaUtil.clasificar(
                BigDecimal.valueOf(50), 100);
        assertThat(c).isEqualTo(ClasificacionGeocerca.OK);
    }

    @Test
    void clasificar_justoEnTolerancia_OK() {
        ClasificacionGeocerca c = GeocercaUtil.clasificar(
                BigDecimal.valueOf(100), 100);
        assertThat(c).isEqualTo(ClasificacionGeocerca.OK);
    }

    @Test
    void clasificar_dentroDeMargenExtendido_SOSPECHOSO() {
        ClasificacionGeocerca c = GeocercaUtil.clasificar(
                BigDecimal.valueOf(150), 100);
        assertThat(c).isEqualTo(ClasificacionGeocerca.SOSPECHOSO);
    }

    @Test
    void clasificar_justoEnMargenExtendido_SOSPECHOSO() {
        ClasificacionGeocerca c = GeocercaUtil.clasificar(
                BigDecimal.valueOf(200), 100);
        assertThat(c).isEqualTo(ClasificacionGeocerca.SOSPECHOSO);
    }

    @Test
    void clasificar_muyLejos_FUERA() {
        ClasificacionGeocerca c = GeocercaUtil.clasificar(
                BigDecimal.valueOf(500), 100);
        assertThat(c).isEqualTo(ClasificacionGeocerca.FUERA);
    }

    @Test
    void clasificar_sinDistancia_SIN_COORDENADAS() {
        ClasificacionGeocerca c = GeocercaUtil.clasificar(null, 100);
        assertThat(c).isEqualTo(ClasificacionGeocerca.SIN_COORDENADAS);
    }

    @Test
    void clasificar_toleranciaNull_usaDefault() {
        ClasificacionGeocerca c = GeocercaUtil.clasificar(
                BigDecimal.valueOf(50), null);
        assertThat(c).isEqualTo(ClasificacionGeocerca.OK);
    }

    @Test
    void clasificar_toleranciaCero_usaDefault() {
        ClasificacionGeocerca c = GeocercaUtil.clasificar(
                BigDecimal.valueOf(50), 0);
        assertThat(c).isEqualTo(ClasificacionGeocerca.OK);
    }

    @Test
    void evaluar_flujoCompleto_devuelveDistanciaYClasificacion() {
        GeocercaUtil.ResultadoGeocerca r = GeocercaUtil.evaluar(
                LAT_UPEU, LON_UPEU, LAT_UPEU, LON_UPEU, 100);
        assertThat(r.distanciaMetros()).isNotNull();
        assertThat(r.clasificacion()).isEqualTo(ClasificacionGeocerca.OK);
    }
}