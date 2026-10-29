package upeu.edu.pe.Saludabemente.aptitudfisica.util.metricas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MetricasAptitudV1 — motor de diagnóstico provisional")
class MetricasAptitudV1Test {

    private MetricasAptitudV1 motor;

    @BeforeEach
    void setUp() {
        motor = new MetricasAptitudV1();
    }

    @Nested
    @DisplayName("dxCore — abdominales 30s")
    class DxCore {

        @Test
        void hombreJovenExcelente() {
            assertThat(motor.dxCore(50, "M", 25)).isEqualTo("EXCELENTE");
        }

        @Test
        void hombreJovenBajo() {
            assertThat(motor.dxCore(10, "M", 25)).isEqualTo("BAJO");
        }

        @Test
        void mujerAdultaPromedio() {
            // Mujer 35a: corte promedio >= 10 → 12 es PROMEDIO
            assertThat(motor.dxCore(12, "F", 35)).isEqualTo("PROMEDIO");
        }

        @Test
        void hombreMayorBueno() {
            // Hombre 60a: corte bueno >= 25 → 26 es MUY_BUENO (>= 25)
            assertThat(motor.dxCore(26, "M", 60)).isEqualTo("MUY_BUENO");
        }
    }

    @Nested
    @DisplayName("dxTrenSuperior — planchas 30s")
    class DxTrenSuperior {

        @Test
        void hombreJovenExcelente() {
            assertThat(motor.dxTrenSuperior(40, "M", 22)).isEqualTo("EXCELENTE");
        }

        @Test
        void mujerJovenBajo() {
            assertThat(motor.dxTrenSuperior(2, "F", 25)).isEqualTo("BAJO");
        }

        @Test
        void hombreAdultoMuyBueno() {
            // Hombre 40a: MUY_BUENO entre 22 y 29 → 25 cumple
            assertThat(motor.dxTrenSuperior(25, "M", 40)).isEqualTo("MUY_BUENO");
        }
    }

    @Nested
    @DisplayName("dxTrenInferior — salto sin impulso")
    class DxTrenInferior {

        @Test
        void hombreJovenExcelente() {
            assertThat(motor.dxTrenInferior(new BigDecimal("210.0"), "M", 25))
                    .isEqualTo("EXCELENTE");
        }

        @Test
        void mujerAdultaPromedio() {
            // Mujer 35a: promedio >= 90 → 100 cae en BUENO (>= 110? no; >= 90 sí)
            assertThat(motor.dxTrenInferior(new BigDecimal("95.0"), "F", 35))
                    .isEqualTo("PROMEDIO");
        }

        @Test
        void hombreMayorBajo() {
            assertThat(motor.dxTrenInferior(new BigDecimal("90.0"), "M", 55))
                    .isEqualTo("BAJO");
        }
    }

    @Nested
    @DisplayName("consolidarFuerza")
    class ConsolidarFuerza {

        @Test
        void tresExcelentes() {
            assertThat(motor.consolidarFuerza("EXCELENTE", "EXCELENTE", "EXCELENTE"))
                    .isEqualTo("EXCELENTE");
        }

        @Test
        void tresBajos() {
            assertThat(motor.consolidarFuerza("BAJO", "BAJO", "BAJO"))
                    .isEqualTo("BAJO");
        }

        @Test
        void mezclaPromedio() {
            // 2 + 3 + 4 = 9 → BUENO (8-10)
            assertThat(motor.consolidarFuerza("PROMEDIO", "BUENO", "MUY_BUENO"))
                    .isEqualTo("BUENO");
        }
    }

    @Nested
    @DisplayName("dxCapacidadCardio")
    class DxCardio {

        @Test
        void hombreJovenExcelente() {
            // soga 100 (EXCELENTE) + carrera 70 (EXCELENTE) → 10 pts → EXCELENTE
            assertThat(motor.dxCapacidadCardio(100, 70, "M", 25))
                    .isEqualTo("EXCELENTE");
        }

        @Test
        void mujerAdultaBajaCapacidad() {
            // soga 10 (BAJO) + carrera 200 (BAJO) → 2 pts → BAJO
            assertThat(motor.dxCapacidadCardio(10, 200, "F", 35))
                    .isEqualTo("BAJO");
        }

        @Test
        void hombreMayorPromedio() {
            // soga 22 (PROMEDIO=2) + carrera 155 (PROMEDIO=2) → 4 → PROMEDIO
            assertThat(motor.dxCapacidadCardio(22, 155, "M", 55))
                    .isEqualTo("PROMEDIO");
        }
    }

    @Nested
    @DisplayName("calcularFitnessScore")
    class FitnessScore {

        @Test
        void todosExcelentesDevuelveCien() {
            BigDecimal score = motor.calcularFitnessScore(
                    "EXCELENTE", "EXCELENTE", "EXCELENTE", "EXCELENTE");
            assertThat(score).isEqualByComparingTo("100.00");
        }

        @Test
        void todosBajosDevuelveVeinte() {
            BigDecimal score = motor.calcularFitnessScore(
                    "BAJO", "BAJO", "BAJO", "BAJO");
            assertThat(score).isEqualByComparingTo("20.00");
        }

        @Test
        void mezclaIntermedia() {
            // Fuerza consolidada EXCELENTE (5) * 0.6 = 3.0
            // Cardio BAJO (1) * 0.4 = 0.4
            // Total = 3.4 * 20 = 68
            BigDecimal score = motor.calcularFitnessScore(
                    "EXCELENTE", "EXCELENTE", "EXCELENTE", "BAJO");
            assertThat(score).isEqualByComparingTo("68.00");
        }
    }

    @Nested
    @DisplayName("Límites y casos borde")
    class Limites {

        @Test
        void ceroAbdominalesEsBajo() {
            assertThat(motor.dxCore(0, "M", 25)).isEqualTo("BAJO");
        }

        @Test
        void edadJustoEnLimite29() {
            assertThat(motor.dxCore(50, "M", 29)).isEqualTo("EXCELENTE");
        }

        @Test
        void edadJustoEnLimite30() {
            // Cambio de rango: 50 reps ya no es excelente para adulto pero sí muy bueno
            assertThat(motor.dxCore(50, "M", 30)).isEqualTo("EXCELENTE");
        }

        @Test
        void edadSetenta() {
            assertThat(motor.dxCore(15, "M", 70)).isEqualTo("PROMEDIO");
        }
    }
}