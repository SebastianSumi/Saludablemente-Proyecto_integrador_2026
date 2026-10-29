package upeu.edu.pe.Saludabemente.nutricional.bioquimico.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.dto.request.BioquimicoExcelDto;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.dto.request.BioquimicoManualRequest;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.dto.response.BioquimicoResponse;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.entity.DetalleBioquimico;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BioquimicoMapper {

    /**
     * Fila 40: fila de Excel → entidad.
     * El Service rellena el vínculo a la evaluación y el dxBioquimico.
     */
    @Mapping(target = "idBioquimico", ignore = true)
    @Mapping(target = "evaluacion", ignore = true)
    @Mapping(target = "dxBioquimico", ignore = true)
    @Mapping(target = "archivoOrigen", source = "archivoOrigen")
    @Mapping(target = "idUsuarioImportador", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "usuarioModificacion", ignore = true)
    DetalleBioquimico toEntityExcel(BioquimicoExcelDto dto, String archivoOrigen);

    /**
     * Fila 41: request manual → entidad.
     */
    @Mapping(target = "idBioquimico", ignore = true)
    @Mapping(target = "evaluacion", ignore = true)
    @Mapping(target = "dxBioquimico", ignore = true)
    @Mapping(target = "archivoOrigen", ignore = true)
    @Mapping(target = "idUsuarioImportador", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "usuarioModificacion", ignore = true)
    DetalleBioquimico toEntityManual(BioquimicoManualRequest request);

    /**
     * Fila 42: entidad → response.
     */
    BioquimicoResponse toResponse(DetalleBioquimico entity);

}