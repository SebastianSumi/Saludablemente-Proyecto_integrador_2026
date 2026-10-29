package upeu.edu.pe.Saludabemente.aptitudfisica.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.aptitudfisica.util.metricas.MetricasAptitud;

import java.math.BigDecimal;

/**
 * Fachada estática-contextual que delega al motor de métricas activo.
 * <p>
 * Los servicios consumen esta clase en lugar de inyectar la interfaz
 * directamente, para que el día que se cambie de implementación
 * ({@code V1 → V2}) no haya que tocar cada inyección.
 */
@Component
@RequiredArgsConstructor
public class MetricasAptitudUtil {

    private final MetricasAptitud motor;

    public String dxCore(int reps, String sexo, int edad) {
        return motor.dxCore(reps, sexo, edad);
    }

    public String dxTrenSuperior(int reps, String sexo, int edad) {
        return motor.dxTrenSuperior(reps, sexo, edad);
    }

    public String dxTrenInferior(BigDecimal saltoCm, String sexo, int edad) {
        return motor.dxTrenInferior(saltoCm, sexo, edad);
    }

    public String consolidarFuerza(String dxCore, String dxSup, String dxInf) {
        return motor.consolidarFuerza(dxCore, dxSup, dxInf);
    }

    public String dxCapacidadCardio(int soga30s, int carrera400mSeg, String sexo, int edad) {
        return motor.dxCapacidadCardio(soga30s, carrera400mSeg, sexo, edad);
    }

    public BigDecimal calcularFitnessScore(String dxCore, String dxSup, String dxInf, String dxCardio) {
        return motor.calcularFitnessScore(dxCore, dxSup, dxInf, dxCardio);
    }
}