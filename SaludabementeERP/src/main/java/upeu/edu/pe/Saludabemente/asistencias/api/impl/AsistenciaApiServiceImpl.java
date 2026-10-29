package upeu.edu.pe.Saludabemente.asistencias.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.asistencias.api.AsistenciaApiService;
import upeu.edu.pe.Saludabemente.asistencias.api.dto.AsistenciaSnapshotDTO;
import upeu.edu.pe.Saludabemente.asistencias.api.dto.HistorialAsistenciaDTO;
import upeu.edu.pe.Saludabemente.asistencias.api.dto.ParticipacionActividadDTO;
import upeu.edu.pe.Saludabemente.asistencias.registro.entity.Asistencia;
import upeu.edu.pe.Saludabemente.asistencias.registro.repository.AsistenciaRepository;
import upeu.edu.pe.Saludabemente.asistencias.reporte.dto.response.ParticipacionActividadResponse;
import upeu.edu.pe.Saludabemente.asistencias.reporte.dto.response.ReportePersonaResponse;
import upeu.edu.pe.Saludabemente.asistencias.reporte.service.ReporteAsistenciasService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AsistenciaApiServiceImpl implements AsistenciaApiService {

    private final AsistenciaRepository asistenciaRepository;
    private final ReporteAsistenciasService reporteAsistenciasService;

    @Override
    @Transactional(readOnly = true)
    public AsistenciaSnapshotDTO obtenerSnapshot(Long idAsistencia) {
        return asistenciaRepository.findById(idAsistencia)
                .map(this::toSnapshot)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaSnapshotDTO> listarPorActividad(Long idActividad) {
        return asistenciaRepository.findByIdActividad(idActividad).stream()
                .map(this::toSnapshot)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HistorialAsistenciaDTO historialResumido(Long idPersona, String periodo) {
        ReportePersonaResponse r = reporteAsistenciasService.reportePorPersona(idPersona, periodo, null, null);
        return new HistorialAsistenciaDTO(
                r.idPersona(), r.rangoConsultado(),
                r.totalActividades(), r.totalAsistio(), r.totalAusente(),
                r.totalExonerado(), r.totalIncompleta(), r.porcentajeCumplimiento());
    }

    @Override
    @Transactional(readOnly = true)
    public ParticipacionActividadDTO tasaParticipacion(Long idActividad) {
        ParticipacionActividadResponse p = reporteAsistenciasService.participacionPorActividad(idActividad);
        return new ParticipacionActividadDTO(
                p.idActividad(), p.totalEsperados(), p.totalAsistentes(),
                p.totalAusentes(), p.totalExonerados(), p.totalCuarentenaPendiente(),
                p.porcentajeParticipacion(), p.metodoMasUsado());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> extraerAsistentesParaGamificacion(Long idActividad) {
        return reporteAsistenciasService.asistentesParaGamificacion(idActividad);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer contarAusentismoCronico(Long idPersona, Integer minRacha) {
        return reporteAsistenciasService.ausentismoCronico(minRacha).contains(idPersona) ? 1 : 0;
    }

    private AsistenciaSnapshotDTO toSnapshot(Asistencia a) {
        return new AsistenciaSnapshotDTO(
                a.getIdAsistencia(),
                a.getIdActividad(),
                a.getIdPersona(),
                a.getTipoAsistencia() != null ? a.getTipoAsistencia().name() : null,
                a.getEstado() != null ? a.getEstado().name() : null,
                a.getHoraMarcado(),
                a.getMetodoRegistro() != null ? a.getMetodoRegistro().name() : null);
    }
}