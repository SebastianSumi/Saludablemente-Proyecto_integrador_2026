package upeu.edu.pe.Saludabemente.noticias.categoria.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import upeu.edu.pe.Saludabemente.noticias.categoria.dto.CategoriaRequestDTO;
import upeu.edu.pe.Saludabemente.noticias.categoria.dto.CategoriaResponseDTO;
import upeu.edu.pe.Saludabemente.noticias.categoria.entity.CategoriaEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoriaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "esSistema", ignore = true)
    @Mapping(target = "activo", ignore = true)
    CategoriaEntity toEntity(CategoriaRequestDTO dto);

    @Mapping(target = "cantidadNoticias", ignore = true)
    CategoriaResponseDTO toResponse(CategoriaEntity entity);
}