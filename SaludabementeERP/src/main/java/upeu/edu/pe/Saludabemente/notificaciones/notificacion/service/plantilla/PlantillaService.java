package upeu.edu.pe.Saludabemente.notificaciones.notificacion.service.plantilla;

import upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto.PlantillaRequest;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto.PlantillaResponse;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto.PlantillaResumen;

import java.util.List;

public interface PlantillaService {

    List<PlantillaResumen> listar();

    PlantillaResponse obtener(Long id);

    PlantillaResponse crear(PlantillaRequest request);

    PlantillaResponse actualizar(Long id, PlantillaRequest request);

    void eliminar(Long id);
}