package upeu.edu.pe.Saludabemente.nutricional.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BioquimicoRulesUtilTest {

    @Test
    void perfilLipidicoNormal() {
        assertThat(BioquimicoRulesUtil.evaluarPerfilLipidico(
                new BigDecimal("180"), new BigDecimal("120")))
                .isEqualTo("NORMAL");
    }

    @Test
    void perfilLipidicoLimitrofe() {
        assertThat(BioquimicoRulesUtil.evaluarPerfilLipidico(
                new BigDecimal("220"), new BigDecimal("120")))
                .isEqualTo("LIMITROFE");
    }

    @Test
    void perfilLipidicoAlto() {
        assertThat(BioquimicoRulesUtil.evaluarPerfilLipidico(
                new BigDecimal("250"), new BigDecimal("120")))
                .isEqualTo("ALTO");
    }

    @Test
    void riesgoGlobalNormalSiTodosLosMarcadoresEstanEnRango() {
        String dx = BioquimicoRulesUtil.calcularRiesgoGlobal(
                new BigDecimal("90"),   // glucosa
                new BigDecimal("180"),  // colesterol
                new BigDecimal("120"),  // triglicéridos
                115,                    // sistólica
                75);                    // diastólica

        assertThat(dx).isEqualTo("NORMAL");
    }

    @Test
    void riesgoGlobalAlteradoConGlucosaYColesterol() {
        String dx = BioquimicoRulesUtil.calcularRiesgoGlobal(
                new BigDecimal("110"),
                new BigDecimal("220"),
                new BigDecimal("120"),
                115,
                75);

        assertThat(dx).isEqualTo("ALTERADO: GLUCOSA, COLESTEROL");
    }

    @Test
    void riesgoGlobalConTodosLosMarcadoresAlterados() {
        String dx = BioquimicoRulesUtil.calcularRiesgoGlobal(
                new BigDecimal("130"),
                new BigDecimal("250"),
                new BigDecimal("200"),
                145,
                95);

        assertThat(dx).isEqualTo("ALTERADO: GLUCOSA, COLESTEROL, TRIGLICERIDOS, PRESION_SISTOLICA, PRESION_DIASTOLICA");
    }

    @Test
    void riesgoGlobalIgnoraNulls() {
        String dx = BioquimicoRulesUtil.calcularRiesgoGlobal(
                null, null, null, 145, null);

        assertThat(dx).isEqualTo("ALTERADO: PRESION_SISTOLICA");
    }
}