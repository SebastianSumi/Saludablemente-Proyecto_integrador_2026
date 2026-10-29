package upeu.edu.pe.Saludabemente.metas.meta.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import upeu.edu.pe.Saludabemente.metas.meta.dto.CheckInRequestDTO;
import upeu.edu.pe.Saludabemente.metas.meta.entity.CheckInEntity;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CheckInMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "meta", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    CheckInEntity toEntity(CheckInRequestDTO dto);
}