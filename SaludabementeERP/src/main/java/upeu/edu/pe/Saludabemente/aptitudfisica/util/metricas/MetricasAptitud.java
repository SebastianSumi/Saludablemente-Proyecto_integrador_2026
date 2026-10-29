package upeu.edu.pe.Saludabemente.aptitudfisica.util.metricas;

import java.math.BigDecimal;

/**
 * Contrato del motor de diagnóstico físico.
 * <p>
 * Recibe valores crudos de las 5 pruebas y devuelve el nivel cualitativo
 * correspondiente, cruzado por sexo y edad. La implementación activa se
 * inyecta vía Spring; este contrato desacopla los servicios de cualquier
 * fuente específica de baremos.
 */
public interface MetricasAptitud {

    /** Abdominales en 30s. */
    String dxCore(int repeticiones, String sexo, int edad);

    /** Flexiones de brazo (planchas) en 30s. */
    String dxTrenSuperior(int repeticiones, String sexo, int edad);

    /** Salto de longitud sin impulso, en cm. */
    String dxTrenInferior(BigDecimal saltoCm, String sexo, int edad);

    /** Consolida los 3 diagnósticos de fuerza en uno solo. */
    String consolidarFuerza(String dxCore, String dxTrenSuperior, String dxTrenInferior);

    /** Salto de soga 30s + carrera 400m. */
    String dxCapacidadCardio(int soga30s, int carrera400mSeg, String sexo, int edad);

    /** Score global 0–100, ponderando los 4 diagnósticos. */
    BigDecimal calcularFitnessScore(String dxCore,
                                    String dxTrenSuperior,
                                    String dxTrenInferior,
                                    String dxCardio);
}