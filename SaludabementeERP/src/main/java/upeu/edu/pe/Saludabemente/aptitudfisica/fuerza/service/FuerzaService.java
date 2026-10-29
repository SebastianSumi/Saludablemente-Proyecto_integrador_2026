package upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.service;

import upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.dto.request.FuerzaRequest;
import upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.dto.response.FuerzaResponse;

public interface FuerzaService {
    FuerzaResponse registrar(Long idEvaluacion, FuerzaRequest request);
    FuerzaResponse actualizar(Long idEvaluacion, FuerzaRequest request);
    FuerzaResponse consultar(Long idEvaluacion);
}