package upeu.edu.pe.Saludabemente.nutricional.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OmronRulesUtilTest {

    // ------------------------------------------------------------------
    // IMC
    // ------------------------------------------------------------------

    @Test
    void calcularImcConDatosDelExcelReal() {
        // Fila 1 del Excel: 150.4 cm, 47.0 kg
        BigDecimal imc = OmronRulesUtil.calcularImc(
                new BigDecimal("47.0"),
                new BigDecimal("150.4"));

        assertThat(imc).isEqualByComparingTo("20.8");
    }

    @Test
    void calcularImcRetornaNullSiFaltanDatos() {
        assertThat(OmronRulesUtil.calcularImc(null, new BigDecimal("150"))).isNull();
        assertThat(OmronRulesUtil.calcularImc(new BigDecimal("50"), null)).isNull();
    }

    @Test
    void calcularImcFallaSiEstaturaEsCero() {
        assertThatThrownBy(() ->
                OmronRulesUtil.calcularImc(new BigDecimal("50"), BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void diagnosticarImcEnLosCuatroRangos() {
        assertThat(OmronRulesUtil.diagnosticarImc(new BigDecimal("17.0"))).isEqualTo("DELGADEZ");
        assertThat(OmronRulesUtil.diagnosticarImc(new BigDecimal("22.0"))).isEqualTo("NORMAL");
        assertThat(OmronRulesUtil.diagnosticarImc(new BigDecimal("27.0"))).isEqualTo("SOBREPESO");
        assertThat(OmronRulesUtil.diagnosticarImc(new BigDecimal("32.0"))).isEqualTo("OBESIDAD");
    }

    // ------------------------------------------------------------------
    // Perímetro abdominal
    // ------------------------------------------------------------------

    @Test
    void diagnosticarPerimetroAbdominalMujer() {
        assertThat(OmronRulesUtil.diagnosticarPerimetroAbdominal(new BigDecimal("75"), "F"))
                .isEqualTo("NORMAL");
        assertThat(OmronRulesUtil.diagnosticarPerimetroAbdominal(new BigDecimal("85"), "F"))
                .isEqualTo("RIESGO");
        assertThat(OmronRulesUtil.diagnosticarPerimetroAbdominal(new BigDecimal("90"), "F"))
                .isEqualTo("RIESGO_ALTO");
    }

    @Test
    void diagnosticarPerimetroAbdominalHombre() {
        assertThat(OmronRulesUtil.diagnosticarPerimetroAbdominal(new BigDecimal("90"), "M"))
                .isEqualTo("NORMAL");
        assertThat(OmronRulesUtil.diagnosticarPerimetroAbdominal(new BigDecimal("98"), "M"))
                .isEqualTo("RIESGO");
        assertThat(OmronRulesUtil.diagnosticarPerimetroAbdominal(new BigDecimal("105"), "M"))
                .isEqualTo("RIESGO_ALTO");
    }

    // ------------------------------------------------------------------
    // Grasa corporal
    // ------------------------------------------------------------------

    @Test
    void diagnosticarGrasaMujerJoven() {
        assertThat(OmronRulesUtil.diagnosticarGrasa(new BigDecimal("20"), "F", 30))
                .isEqualTo("BAJO");
        assertThat(OmronRulesUtil.diagnosticarGrasa(new BigDecimal("28"), "F", 30))
                .isEqualTo("NORMAL");
        assertThat(OmronRulesUtil.diagnosticarGrasa(new BigDecimal("36"), "F", 30))
                .isEqualTo("ALTO");
        assertThat(OmronRulesUtil.diagnosticarGrasa(new BigDecimal("42"), "F", 30))
                .isEqualTo("MUY_ALTO");
    }

    @Test
    void diagnosticarGrasaHombreJoven() {
        assertThat(OmronRulesUtil.diagnosticarGrasa(new BigDecimal("7"), "M", 30))
                .isEqualTo("BAJO");
        assertThat(OmronRulesUtil.diagnosticarGrasa(new BigDecimal("15"), "M", 30))
                .isEqualTo("NORMAL");
        assertThat(OmronRulesUtil.diagnosticarGrasa(new BigDecimal("22"), "M", 30))
                .isEqualTo("ALTO");
        assertThat(OmronRulesUtil.diagnosticarGrasa(new BigDecimal("28"), "M", 30))
                .isEqualTo("MUY_ALTO");
    }

    // ------------------------------------------------------------------
    // Masa muscular
    // ------------------------------------------------------------------

    @Test
    void diagnosticarMasaMuscularMujerJoven() {
        assertThat(OmronRulesUtil.diagnosticarMasaMuscular(new BigDecimal("22"), "F", 30))
                .isEqualTo("BAJO");
        assertThat(OmronRulesUtil.diagnosticarMasaMuscular(new BigDecimal("27"), "F", 30))
                .isEqualTo("NORMAL");
        assertThat(OmronRulesUtil.diagnosticarMasaMuscular(new BigDecimal("33"), "F", 30))
                .isEqualTo("ALTO");
    }

    @Test
    void diagnosticarMasaMuscularHombreJoven() {
        assertThat(OmronRulesUtil.diagnosticarMasaMuscular(new BigDecimal("30"), "M", 30))
                .isEqualTo("BAJO");
        assertThat(OmronRulesUtil.diagnosticarMasaMuscular(new BigDecimal("37"), "M", 30))
                .isEqualTo("NORMAL");
        assertThat(OmronRulesUtil.diagnosticarMasaMuscular(new BigDecimal("42"), "M", 30))
                .isEqualTo("ALTO");
    }

    // ------------------------------------------------------------------
    // Grasa visceral
    // ------------------------------------------------------------------

    @Test
    void diagnosticarGrasaVisceral() {
        assertThat(OmronRulesUtil.diagnosticarGrasaVisceral(new BigDecimal("8")))
                .isEqualTo("NORMAL");
        assertThat(OmronRulesUtil.diagnosticarGrasaVisceral(new BigDecimal("12")))
                .isEqualTo("ALTO");
        assertThat(OmronRulesUtil.diagnosticarGrasaVisceral(new BigDecimal("18")))
                .isEqualTo("MUY_ALTO");
    }
}