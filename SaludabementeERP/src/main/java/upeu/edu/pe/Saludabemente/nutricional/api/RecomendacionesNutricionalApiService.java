package upeu.edu.pe.Saludabemente.nutricional.api;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Contrato de solo lectura del módulo Nutricional hacia Recomendaciones IA.
 */
public interface RecomendacionesNutricionalApiService {

    /**
     * Cabecera de la evaluación.
     * @throws upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException si no existe
     */
    EvaluacionNutricionalDto obtenerCabecera(Long idEvaluacion);

    /**
     * Antropometría con diagnósticos OMRON ya calculados.
     * @throws upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException
     *         si la evaluación no existe o no tiene antropometría
     */
    AntropometricoDto obtenerAntropometria(Long idEvaluacion);

    /**
     * Bioquímica con diagnóstico global. Vacío si no hay laboratorio.
     */
    Optional<BioquimicoDto> obtenerBioquimica(Long idEvaluacion);

    /**
     * Evaluación inmediatamente anterior a la fecha indicada.
     * Vacío si no existe.
     */
    Optional<EvaluacionHistoricaDto> obtenerEvaluacionAnterior(Long idPersona,
                                                               LocalDate fechaReferencia);

    /**
     * true si tiene antropometría y bioquímica registradas.
     */
    boolean estaCompleta(Long idEvaluacion);
}