package upeu.edu.pe.Saludabemente.actividades.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.actividades.agenda.entity.Actividad;
import upeu.edu.pe.Saludabemente.actividades.agenda.repository.ActividadRepository;
import upeu.edu.pe.Saludabemente.actividades.api.ActividadApiService;
import upeu.edu.pe.Saludabemente.actividades.api.dto.ActividadSnapshotDto;
import upeu.edu.pe.Saludabemente.actividades.inscripcion.service.InscripcionService;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;


import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActividadApiServiceImpl implements ActividadApiService {

    private final ActividadRepository actividadRepository;
    private final InscripcionService inscripcionService;

    @Override
    @Transactional(readOnly = true)
    public ActividadSnapshotDto obtenerSnapshot(Long idActividad) {
        return actividadRepository.findById(idActividad)
                .map(this::toSnapshot)
                .orElse(null);   // ← devuelve null si no existe, NO lanza
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActividadSnapshotDto> listarEnCurso() {
        return actividadRepository.findByEstado("EN_CURSO")
                .stream()
                .map(this::toSnapshot)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActividadSnapshotDto> listarPorRango(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            return List.of();
        }
        return actividadRepository.findBetweenDates(desde, hasta)
                .stream()
                .map(this::toSnapshot)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActividadSnapshotDto> obtenerSnapshots(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return actividadRepository.findAllById(ids)
                .stream()
                .map(this::toSnapshot)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActividadSnapshotDto> listarFinalizadasNoCerradas() {
        return actividadRepository.findFinalizadasNoCerradas()
                .stream()
                .map(this::toSnapshot)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> listarInscritos(Long idActividad) {
        long total = inscripcionService.contarInscritos(idActividad);
        if (total == 0) {
            // Sin inscritos explicitos -> Asistencias usa "todo el personal activo"
            return null;
        }
        return inscripcionService.listarInscritos(idActividad);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaInscrito(Long idActividad, Long idPersona) {
        return inscripcionService.estaInscrito(idActividad, idPersona);
    }

    @Override
    @Transactional
    public void marcarCierreRealizado(Long idActividad) {
        Actividad a = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Actividad no encontrada: " + idActividad));
        a.setCierreAsistenciasRealizado(true);
        actividadRepository.save(a);
    }

    // ============================================================
    // helpers
    // ============================================================
    private ActividadSnapshotDto toSnapshot(Actividad a) {
        return new ActividadSnapshotDto(
                a.getIdActividad(),
                a.getNombre(),
                a.getDescripcion(),
                a.getEstado(),
                a.getFechaActividad(),
                a.getHoraInicio(),
                a.getHoraFin(),
                a.getLugar(),
                a.getLatitudOficial(),
                a.getLongitudOficial(),
                a.getToleranciaGeocercaMetros(),
                a.getRequiereCheckInOut(),
                a.getDuracionMinutos(),
                a.getPeriodo(),
                a.getIdUsuarioCreador(),
                a.getPermiteAutoservicio(),
                a.getPermiteEscaneoCoordinador()
        );
    }
}