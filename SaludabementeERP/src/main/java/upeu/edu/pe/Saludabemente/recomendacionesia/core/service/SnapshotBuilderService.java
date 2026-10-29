package upeu.edu.pe.Saludabemente.recomendacionesia.core.service;

import upeu.edu.pe.Saludabemente.recomendacionesia.core.dto.SnapshotDto;

public interface SnapshotBuilderService {

    /**
     * Arma el snapshot completo de un colaborador para una evaluación.
     * Llama a los servicios de Sebastián (Nutricional, Aptitud Física, Personal).
     */
    SnapshotDto construir(Long idEvaluacion, Long idPersona);
}