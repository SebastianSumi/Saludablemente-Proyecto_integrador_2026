package upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.dto.request.EvaluacionInitRequest;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.dto.request.ObservacionesRequest;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.dto.response.EvaluacionAptitudResponse;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.dto.resumen.AptitudAgregadoDto;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.dto.resumen.EvaluacionResumen;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.entity.EvaluacionAptitud;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.enums.EstadoEvaluacion;

import java.util.List;

public interface EvaluacionAptitudService {

    // ---- Consultas ----
    Page<EvaluacionResumen> buscar(Long idPeriodo, EstadoEvaluacion estado,
                                   String area, String texto, Pageable pageable);
    EvaluacionAptitudResponse obtener(Long id);
    List<EvaluacionResumen> historial(Long idPersona);
    AptitudAgregadoDto obtenerAgregados(Long idPeriodo);

    // ---- Comandos ----
    EvaluacionAptitudResponse iniciar(EvaluacionInitRequest request);
    void anular(Long id);
    EvaluacionAptitudResponse actualizarObservaciones(Long id, ObservacionesRequest request);

    // ---- Interno pero expuesto a submódulos fuerza/cardio ----
    /**
     * Re-evalúa la completitud de la cabecera: si tiene detalle de fuerza
     * y de cardio, calcula dictamen global, fitness score y pasa a COMPLETA.
     * Es invocado por {@code FuerzaServiceImpl} y {@code CardioServiceImpl}
     * luego de persistir su detalle.
     */
    void evaluarCompletitud(Long idEvaluacion);

    // ---- Uso interno ----
    EvaluacionAptitud buscarOFallar(Long id);
}