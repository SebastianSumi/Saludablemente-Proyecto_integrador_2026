package upeu.edu.pe.Saludabemente.asistencias.registro.service;

import upeu.edu.pe.Saludabemente.asistencias.registro.dto.request.AsistenciaManualRequest;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.request.AsistenciaQrCoordinadorRequest;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.request.MarcacionLoteRequest;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.request.MarcacionRequest;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.response.AsistenciaHistorialResponse;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.response.AsistenciaResponse;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.response.CierreAsistenciasResponse;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.response.MarcacionLoteResponse;

import java.util.List;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.request.AsistenciaManualDetalleRequest;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.response.AsistenciaActividadResponse;

public interface AsistenciaService {

    AsistenciaActividadResponse guardarManualDetalle(AsistenciaManualDetalleRequest request);

    AsistenciaActividadResponse consultarDetalle(Long idActividad);

    AsistenciaResponse registrarManual(AsistenciaManualRequest request);

    /** Marcación del wizard (upsert, solo EN_CURSO). */
    AsistenciaResponse marcar(MarcacionRequest request);

    /** Marcación masiva (todos PRESENTE, solo EN_CURSO). */
    MarcacionLoteResponse marcarLote(MarcacionLoteRequest request);

    List<AsistenciaResponse> listarPorActividad(Long idActividad);

    List<AsistenciaHistorialResponse> historialPorPersona(Long idPersona);
    AsistenciaResponse registrarPorQrCoordinador(AsistenciaQrCoordinadorRequest request);

    CierreAsistenciasResponse generarCierreDeAsistencias(Long idActividad);

    void calcularTiempoEfectivo(Long idActividad, Long idPersona);
}
