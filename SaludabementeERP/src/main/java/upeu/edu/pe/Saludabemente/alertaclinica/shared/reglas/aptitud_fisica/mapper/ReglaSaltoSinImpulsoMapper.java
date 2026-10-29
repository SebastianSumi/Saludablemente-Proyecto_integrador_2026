package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaSaltoSinImpulsoRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaSaltoSinImpulsoResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.entity.ReglaSaltoSinImpulso;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReglaSaltoSinImpulsoMapper {

    @Mapping(target = "idReglaSalto", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    ReglaSaltoSinImpulso toEntity(ReglaSaltoSinImpulsoRequestDto requestDto);

    ReglaSaltoSinImpulsoResponseDto toResponseDto(ReglaSaltoSinImpulso entity);

    List<ReglaSaltoSinImpulsoResponseDto> toResponseDtoList(List<ReglaSaltoSinImpulso> entities);

    @Mapping(target = "idReglaSalto", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    void updateEntityFromDto(ReglaSaltoSinImpulsoRequestDto requestDto,
                             @MappingTarget ReglaSaltoSinImpulso entity);
}