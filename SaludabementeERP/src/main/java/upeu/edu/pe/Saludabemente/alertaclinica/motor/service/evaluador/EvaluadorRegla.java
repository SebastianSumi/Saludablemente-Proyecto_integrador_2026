package upeu.edu.pe.Saludabemente.alertaclinica.motor.service.evaluador;

import upeu.edu.pe.Saludabemente.alertaclinica.integracion.dto.Hallazgo;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Contrato que implementa cada evaluador interno del motor.
 * Cada evaluador sabe que tipos de metrica atiende, y como cruzarlos
 * contra el catalogo de reglas activas.
 */
public interface EvaluadorRegla {

    /**
     * Tipos de metrica que este evaluador procesa.
     * Ejemplo: List.of("IMC") o List.of("PRESION_SISTOLICA", "PRESION_DIASTOLICA").
     */
    List<String> tiposSoportados();

    /**
     * Evalua las metricas y devuelve los hallazgos encontrados.
     * Devuelve lista vacia si ninguna metrica esta fuera de rango.
     *
     * @param sexo      sexo biologico del paciente
     * @param edad      edad calculada a la fecha de la evaluacion
     * @param metricas  mapa de tipo de metrica -> valor medido
     */
    List<Hallazgo> evaluar(SexoBiologico sexo, int edad, Map<String, BigDecimal> metricas);
}