package upeu.edu.pe.Saludabemente.notificaciones.notificacion.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto.PlantillaRequest;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto.PlantillaResponse;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto.PlantillaResumen;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.entity.PlantillaMensaje;

@Mapper(componentModel = "spring")
public interface PlantillaMapper {

    @Mapping(target = "idPlantilla", ignore = true)
    PlantillaMensaje toEntity(PlantillaRequest request);

    PlantillaResponse toResponse(PlantillaMensaje plantilla);

    PlantillaResumen toResumen(PlantillaMensaje plantilla);
}