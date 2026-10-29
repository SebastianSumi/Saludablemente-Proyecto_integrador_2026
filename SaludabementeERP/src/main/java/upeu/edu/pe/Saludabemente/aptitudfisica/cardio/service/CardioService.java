package upeu.edu.pe.Saludabemente.aptitudfisica.cardio.service;

import upeu.edu.pe.Saludabemente.aptitudfisica.cardio.dto.request.CardioRequest;
import upeu.edu.pe.Saludabemente.aptitudfisica.cardio.dto.response.CardioResponse;

public interface CardioService {
    CardioResponse registrar(Long idEvaluacion, CardioRequest request);
    CardioResponse actualizar(Long idEvaluacion, CardioRequest request);
    CardioResponse consultar(Long idEvaluacion);
}