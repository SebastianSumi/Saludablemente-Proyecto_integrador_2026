package upeu.edu.pe.Saludabemente.nutricional.antropometrico.service;

import upeu.edu.pe.Saludabemente.nutricional.antropometrico.dto.request.AntropometricoRequest;
import upeu.edu.pe.Saludabemente.nutricional.antropometrico.dto.response.AntropometricoResponse;

/**
 * Gestión del detalle antropométrico de una evaluación nutricional.
 * Cubre las funciones 26-28 de PLANIFICACION.docx.
 *
 * <p>Este servicio es <b>interno al módulo nutricional</b>. Se invoca desde
 * {@code AntropometricoController} o desde otros puntos del propio módulo.
 * La exposición a otros módulos del sistema se decide en Fase 9.</p>
 */
public interface AntropometricoService {

    /**
     * Registra las medidas físicas de una evaluación PENDIENTE.
     * Calcula los diagnósticos OMRON con el sexo y la edad congelados en el
     * snapshot. Al finalizar, evalúa la completitud de la evaluación.
     */
    AntropometricoResponse registrar(Long idEvaluacion, AntropometricoRequest request);

    /**
     * Recalcula los diagnósticos OMRON sobre las medidas ya guardadas.
     * Uso: RRHH corrigió la fecha de nacimiento o el sexo de la persona.
     */
    AntropometricoResponse recalcular(Long idEvaluacion);

    // En AntropometricoService
    AntropometricoResponse actualizar(Long idEvaluacion, AntropometricoRequest request);
}