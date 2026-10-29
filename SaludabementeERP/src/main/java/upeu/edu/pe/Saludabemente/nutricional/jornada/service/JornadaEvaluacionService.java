package upeu.edu.pe.Saludabemente.nutricional.jornada.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import upeu.edu.pe.Saludabemente.nutricional.jornada.dto.request.JornadaEvaluacionRequest;
import upeu.edu.pe.Saludabemente.nutricional.jornada.dto.response.InscripcionJornadaResponse;
import upeu.edu.pe.Saludabemente.nutricional.jornada.dto.response.JornadaEvaluacionResponse;
import upeu.edu.pe.Saludabemente.nutricional.jornada.enums.EstadoInscripcion;
import upeu.edu.pe.Saludabemente.nutricional.jornada.enums.EstadoJornada;

import java.util.List;

public interface JornadaEvaluacionService {

    // ---- Consultas ----
    Page<JornadaEvaluacionResponse> buscar(EstadoJornada estado, Long idPeriodo, Pageable pageable);

    JornadaEvaluacionResponse obtener(Long idJornada);

    List<InscripcionJornadaResponse> listarInscritos(Long idJornada);

    List<InscripcionJornadaResponse> misInscripciones();

    // ---- Administración ----
    JornadaEvaluacionResponse crear(JornadaEvaluacionRequest request);

    JornadaEvaluacionResponse actualizar(Long idJornada, JornadaEvaluacionRequest request);

    JornadaEvaluacionResponse cambiarEstado(Long idJornada, EstadoJornada estado);

    InscripcionJornadaResponse cambiarEstadoInscripcion(
            Long idJornada, Long idInscripcion, EstadoInscripcion estado);

    // ---- Autoservicio ----
    InscripcionJornadaResponse inscribirme(Long idJornada);
}
