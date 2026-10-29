package upeu.edu.pe.Saludabemente.catalogo.area.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.request.AreaRequest;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResponse;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResumen;
import upeu.edu.pe.Saludabemente.catalogo.area.entity.AreaTrabajo;

@Mapper(componentModel = "spring")
public interface AreaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    AreaTrabajo toEntity(AreaRequest request);

    AreaResponse toResponse(AreaTrabajo area);

    AreaResumen toResumen(AreaTrabajo area);
}