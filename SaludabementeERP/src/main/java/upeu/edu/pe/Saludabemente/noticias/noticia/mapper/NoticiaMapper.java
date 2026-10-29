package upeu.edu.pe.Saludabemente.noticias.noticia.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import upeu.edu.pe.Saludabemente.noticias.noticia.dto.NoticiaRequestDTO;
import upeu.edu.pe.Saludabemente.noticias.noticia.dto.NoticiaResponseDTO;
import upeu.edu.pe.Saludabemente.noticias.noticia.entity.NoticiaEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NoticiaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "imagenUrl", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaPublicacion", ignore = true)
    NoticiaEntity toEntity(NoticiaRequestDTO dto);

    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "area", ignore = true)
    @Mapping(target = "teams", ignore = true)
    @Mapping(target = "nombreAutor", ignore = true)
    @Mapping(target = "estado", ignore = true)
    NoticiaResponseDTO toResponse(NoticiaEntity entity);
}