package upeu.edu.pe.Saludabemente.actividades.configuracion.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.actividades.agenda.entity.Actividad;
import upeu.edu.pe.Saludabemente.actividades.agenda.repository.ActividadRepository;
import upeu.edu.pe.Saludabemente.actividades.configuracion.dto.request.GamificacionRequest;
import upeu.edu.pe.Saludabemente.actividades.configuracion.dto.request.MaterialesRequest;
import upeu.edu.pe.Saludabemente.actividades.configuracion.dto.response.ConfiguracionResponse;
import upeu.edu.pe.Saludabemente.actividades.configuracion.entity.ActividadMaterial;
import upeu.edu.pe.Saludabemente.actividades.configuracion.repository.ActividadMaterialRepository;
import upeu.edu.pe.Saludabemente.actividades.configuracion.service.ConfiguracionService;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;


import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfiguracionServiceImpl implements ConfiguracionService {

    private final ActividadRepository actividadRepository;
    private final ActividadMaterialRepository materialRepository;

    @Override
    @Transactional
    public ConfiguracionResponse gestionarMateriales(Long idActividad, MaterialesRequest request) {
        Actividad a = obtenerActividad(idActividad);

        materialRepository.deleteByIdActividad(idActividad);

        List<ActividadMaterial> nuevos = request.materiales().stream()
                .filter(s -> s != null && !s.isBlank())
                .map(s -> new ActividadMaterial(idActividad, s.trim()))
                .toList();
        materialRepository.saveAll(nuevos);

        return toResponse(a);
    }

    @Override
    @Transactional
    public ConfiguracionResponse configurarPuntaje(Long idActividad, GamificacionRequest request) {
        Actividad a = obtenerActividad(idActividad);
        a.setPuntajeSalud(request.puntos());
        actividadRepository.save(a);
        return toResponse(a);
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionResponse obtenerConfiguracion(Long idActividad) {
        Actividad a = obtenerActividad(idActividad);
        return toResponse(a);
    }

    // ===================== helpers =====================

    private Actividad obtenerActividad(Long id) {
        return actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada: " + id));
    }

    private ConfiguracionResponse toResponse(Actividad a) {
        Long idActividad = a.getIdActividad();

        List<String> materiales = materialRepository.findByIdActividad(idActividad)
                .stream()
                .map(ActividadMaterial::getDescripcion)
                .toList();

        return new ConfiguracionResponse(
                idActividad,
                a.getPuntajeSalud(),
                materiales
        );
    }
}