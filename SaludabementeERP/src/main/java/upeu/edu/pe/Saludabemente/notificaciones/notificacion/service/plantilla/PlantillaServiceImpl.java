package upeu.edu.pe.Saludabemente.notificaciones.notificacion.service.plantilla;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto.PlantillaRequest;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto.PlantillaResponse;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto.PlantillaResumen;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.entity.PlantillaMensaje;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.mapper.PlantillaMapper;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.repository.PlantillaMensajeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlantillaServiceImpl implements PlantillaService {

    private final PlantillaMensajeRepository plantillaRepository;
    private final PlantillaMapper plantillaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PlantillaResumen> listar() {
        return plantillaRepository.findAll()
                .stream()
                .map(plantillaMapper::toResumen)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PlantillaResponse obtener(Long id) {
        return plantillaMapper.toResponse(buscarOFallar(id));
    }

    @Override
    @Transactional
    public PlantillaResponse crear(PlantillaRequest request) {
        plantillaRepository.findByCodigoEvento(request.getCodigoEvento())
                .ifPresent(p -> {
                    throw new BusinessRuleException(
                            "Ya existe una plantilla con el código de evento: "
                                    + request.getCodigoEvento());
                });

        PlantillaMensaje plantilla = plantillaMapper.toEntity(request);
        if (plantilla.getActivo() == null) {
            plantilla.setActivo(Boolean.TRUE);
        }
        return plantillaMapper.toResponse(plantillaRepository.save(plantilla));
    }

    @Override
    @Transactional
    public PlantillaResponse actualizar(Long id, PlantillaRequest request) {
        PlantillaMensaje plantilla = buscarOFallar(id);

        plantillaRepository.findByCodigoEvento(request.getCodigoEvento())
                .ifPresent(otra -> {
                    if (!otra.getIdPlantilla().equals(id)) {
                        throw new BusinessRuleException(
                                "Otra plantilla ya usa el código de evento: "
                                        + request.getCodigoEvento());
                    }
                });

        plantilla.setCodigoEvento(request.getCodigoEvento());
        plantilla.setCanal(request.getCanal());
        plantilla.setAsunto(request.getAsunto());
        plantilla.setCuerpoHtml(request.getCuerpoHtml());
        if (request.getActivo() != null) {
            plantilla.setActivo(request.getActivo());
        }
        return plantillaMapper.toResponse(plantillaRepository.save(plantilla));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        plantillaRepository.delete(buscarOFallar(id));
    }

    private PlantillaMensaje buscarOFallar(Long id) {
        return plantillaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plantilla no encontrada: " + id));
    }
}