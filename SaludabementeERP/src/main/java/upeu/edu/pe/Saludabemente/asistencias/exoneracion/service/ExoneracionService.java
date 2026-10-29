package upeu.edu.pe.Saludabemente.asistencias.exoneracion.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import upeu.edu.pe.Saludabemente.asistencias.enums.EstadoJustificacion;
import upeu.edu.pe.Saludabemente.asistencias.exoneracion.dto.request.DictamenRequest;
import upeu.edu.pe.Saludabemente.asistencias.exoneracion.dto.request.JustificacionRequest;
import upeu.edu.pe.Saludabemente.asistencias.exoneracion.dto.response.JustificacionResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface ExoneracionService {

    JustificacionResponse solicitar(JustificacionRequest request);

    JustificacionResponse dictaminar(Long idJustificacion, DictamenRequest request);

    Page<JustificacionResponse> listar(
            EstadoJustificacion estado,
            Long idPersona,
            LocalDateTime desde,
            LocalDateTime hasta,
            Pageable pageable);

    List<Long> detectarAusentismoCronico(int minRacha);
}