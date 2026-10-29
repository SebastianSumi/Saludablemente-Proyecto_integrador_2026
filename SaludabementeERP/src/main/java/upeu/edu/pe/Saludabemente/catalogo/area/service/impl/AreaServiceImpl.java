package upeu.edu.pe.Saludabemente.catalogo.area.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.request.AreaRequest;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResponse;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResumen;
import upeu.edu.pe.Saludabemente.catalogo.area.entity.AreaTrabajo;
import upeu.edu.pe.Saludabemente.catalogo.area.mapper.AreaMapper;
import upeu.edu.pe.Saludabemente.catalogo.area.repository.AreaRepository;
import upeu.edu.pe.Saludabemente.catalogo.area.service.AreaService;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AreaServiceImpl implements AreaService {

    private final AreaRepository areaRepository;
    private final AreaMapper areaMapper;

    // -------------------------------------------------------------------
    // Consultas internas
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<AreaResponse> buscar(Boolean activo, Sort sort) {
        List<AreaTrabajo> areas;
        if (activo == null) {
            areas = areaRepository.findAll(sort);
        } else if (activo) {
            areas = areaRepository.findByActivoTrue(sort);
        } else {
            areas = areaRepository.findByActivoFalse(sort);
        }
        return areas.stream().map(areaMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AreaResponse obtener(Long id) {
        return areaMapper.toResponse(buscarOFallar(id));
    }

    // -------------------------------------------------------------------
    // Servicios publicos
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public AreaTrabajo buscarOFallar(Long id) {
        return areaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Area de trabajo no encontrada: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaResumen> listarResumen() {
        Sort sort = Sort.by(Sort.Direction.ASC, "nombre");
        return areaRepository.findByActivoTrue(sort).stream()
                .map(areaMapper::toResumen)
                .toList();
    }

    /**
     * Devuelve el resumen de un area por su id.
     * No lanza excepcion: usa Optional.empty() si no existe.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<AreaResumen> obtenerAreaResumen(Long idArea) {
        if (idArea == null) return Optional.empty();
        return areaRepository.findById(idArea).map(areaMapper::toResumen);
    }

    // -------------------------------------------------------------------
    // Comandos
    // -------------------------------------------------------------------

    @Override
    @Transactional
    public AreaResponse crear(AreaRequest request) {
        validarNombreUnico(request.nombre());

        AreaTrabajo area = areaMapper.toEntity(request);
        area.setActivo(true);
        // AuditableEntity rellena fechaCreacion y usuarioCreacion en @PrePersist.

        return areaMapper.toResponse(areaRepository.save(area));
    }

    @Override
    @Transactional
    public AreaResponse actualizar(Long id, AreaRequest request) {
        AreaTrabajo area = buscarOFallar(id);

        if (!area.getNombre().equals(request.nombre())) {
            validarNombreUnico(request.nombre());
        }

        area.setNombre(request.nombre());
        area.setDescripcion(request.descripcion());
        // AuditableEntity rellena fechaModificacion y usuarioModificacion en @PreUpdate.

        return areaMapper.toResponse(areaRepository.save(area));
    }

    @Override
    @Transactional
    public void inactivar(Long id) {
        AreaTrabajo area = buscarOFallar(id);

        if (!Boolean.TRUE.equals(area.getActivo())) {
            throw new BusinessRuleException("El area ya esta inactiva: " + area.getNombre());
        }

        area.setActivo(false);
        areaRepository.save(area);
    }

    // -------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------

    private void validarNombreUnico(String nombre) {
        if (areaRepository.existsByNombreAndActivoTrue(nombre)) {
            throw new BusinessRuleException(
                    "Ya existe un area activa con el nombre: " + nombre);
        }
    }
}