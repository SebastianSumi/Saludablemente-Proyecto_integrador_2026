package upeu.edu.pe.Saludabemente.asistencias.cuarentena.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import upeu.edu.pe.Saludabemente.asistencias.cuarentena.dto.request.ResolverCuarentenaRequest;
import upeu.edu.pe.Saludabemente.asistencias.cuarentena.dto.response.CuarentenaResponse;
import upeu.edu.pe.Saludabemente.asistencias.enums.EstadoCuarentena;
import upeu.edu.pe.Saludabemente.asistencias.enums.MotivoCuarentena;

import java.time.LocalDateTime;

public interface CuarentenaService {

    Page<CuarentenaResponse> listarBandeja(
            EstadoCuarentena estado,
            MotivoCuarentena motivo,
            Long idActividad,
            Long idPersona,
            LocalDateTime desde,
            LocalDateTime hasta,
            Pageable pageable);

    CuarentenaResponse obtener(Long idCuarentena);

    CuarentenaResponse resolver(Long idCuarentena, ResolverCuarentenaRequest request);

    long contarPendientes();
}