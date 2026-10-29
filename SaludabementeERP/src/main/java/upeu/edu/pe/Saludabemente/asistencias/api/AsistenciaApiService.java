package upeu.edu.pe.Saludabemente.asistencias.api;

import upeu.edu.pe.Saludabemente.asistencias.api.dto.AsistenciaSnapshotDTO;
import upeu.edu.pe.Saludabemente.asistencias.api.dto.HistorialAsistenciaDTO;
import upeu.edu.pe.Saludabemente.asistencias.api.dto.ParticipacionActividadDTO;

import java.util.List;

public interface AsistenciaApiService {

    AsistenciaSnapshotDTO obtenerSnapshot(Long idAsistencia);

    List<AsistenciaSnapshotDTO> listarPorActividad(Long idActividad);

    HistorialAsistenciaDTO historialResumido(Long idPersona, String periodo);

    ParticipacionActividadDTO tasaParticipacion(Long idActividad);

    List<Long> extraerAsistentesParaGamificacion(Long idActividad);

    Integer contarAusentismoCronico(Long idPersona, Integer minRacha);
}