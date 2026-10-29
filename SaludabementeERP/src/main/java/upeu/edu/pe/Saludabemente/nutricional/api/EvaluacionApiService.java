package upeu.edu.pe.Saludabemente.nutricional.api;

import java.util.List;

public interface EvaluacionApiService {

    EvaluacionResumenDTO obtenerResumen(Long idEvaluacion);

    List<EvaluacionResumenDTO> historialPorPersona(Long idPersona);
}