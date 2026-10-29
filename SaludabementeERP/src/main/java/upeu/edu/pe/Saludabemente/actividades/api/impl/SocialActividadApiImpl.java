package upeu.edu.pe.Saludabemente.actividades.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.actividades.agenda.entity.Actividad;
import upeu.edu.pe.Saludabemente.actividades.agenda.repository.ActividadRepository;
import upeu.edu.pe.Saludabemente.actividades.imagen.repository.ActividadImagenRepository;
import upeu.edu.pe.Saludabemente.actividades.api.social.*;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialUsuarioApi;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialActividadApiImpl implements SocialActividadApi {
    private final ActividadRepository actividades;
    private final ActividadImagenRepository imagenes;
    private final SocialUsuarioApi usuarios;

    @Override
    public Optional<ActividadSnapshotDto> obtenerActividadPorId(Long idActividad) {
        return idActividad == null ? Optional.empty() : actividades.findById(idActividad).map(this::snapshot);
    }

    @Override
    public List<ActividadBusquedaDto> buscarActividades(String query, int limite) {
        if (query == null || query.isBlank()) return List.of();
        return actividades.buscarParaSocial(query.trim(), PageRequest.of(0, Math.clamp(limite, 1, 20)))
                .stream().map(a -> new ActividadBusquedaDto(a.getIdActividad(), a.getNombre(),
                        a.getEstado(), a.getFechaActividad())).toList();
    }

    @Override
    public List<ActividadSnapshotDto> actividadesPorEquipo(Long idEquipo) {
        return idEquipo == null ? List.of() : actividades
                .findByIdEquipoActividadOrderByFechaActividadDescIdActividadDesc(idEquipo)
                .stream().map(this::snapshot).toList();
    }

    @Override
    public Optional<ActividadPublicacionDto> obtenerPublicacion(Long idActividad) {
        if (idActividad == null) return Optional.empty();
        return actividades.findById(idActividad).map(a -> {
            String coordinador = null;
            if (a.getIdUsuarioCoordinador() != null) {
                try { coordinador = usuarios.obtenerPorId(a.getIdUsuarioCoordinador()).nombreCompleto(); }
                catch (ResourceNotFoundException ausente) { /* identidad ausente: campo opcional */ }
            }
            return new ActividadPublicacionDto(a.getIdActividad(), a.getNombre(), a.getDescripcion(),
                    a.getLugar(), a.getFechaActividad(), a.getHoraInicio(), a.getHoraFin(), a.getEstado(),
                    a.getLogoUrl(), imagenes.findByIdActividadOrderByOrdenAsc(idActividad).stream()
                        .map(i -> i.getImagenUrl()).toList(), coordinador,
                    a.getIdUsuarioCreador(), a.getIdEquipoActividad());
        });
    }

    private ActividadSnapshotDto snapshot(Actividad a) {
        return new ActividadSnapshotDto(a.getIdActividad(), a.getNombre(), a.getEstado(),
                a.getFechaActividad(), a.getLogoUrl());
    }
}
