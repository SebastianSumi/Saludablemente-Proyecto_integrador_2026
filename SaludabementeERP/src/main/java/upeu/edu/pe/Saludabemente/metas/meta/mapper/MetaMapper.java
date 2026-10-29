package upeu.edu.pe.Saludabemente.metas.meta.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import upeu.edu.pe.Saludabemente.metas.meta.dto.MetaClinicaRequestDTO;
import upeu.edu.pe.Saludabemente.metas.meta.dto.MetaRequestDTO;
import upeu.edu.pe.Saludabemente.metas.meta.dto.MetaResponseDTO;
import upeu.edu.pe.Saludabemente.metas.meta.entity.MetaEntity;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MetaMapper {

    // -----------------------------------------------------------------
    // PERSONAL: MetaRequestDTO -> MetaEntity
    // -----------------------------------------------------------------
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "idEvaluacionOrigen", ignore = true)
    @Mapping(target = "idAlertaOrigen", ignore = true)
    @Mapping(target = "idProfesionalPrescriptor", ignore = true)
    @Mapping(target = "tipoOrigen", ignore = true)
    @Mapping(target = "valorActual", ignore = true)
    @Mapping(target = "fechaCierre", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    @Mapping(target = "modificadoEn", ignore = true)
    @Mapping(target = "checkIns", ignore = true)
    MetaEntity toEntityFromPersonal(MetaRequestDTO dto);

    // -----------------------------------------------------------------
    // CLINICA: MetaClinicaRequestDTO -> MetaEntity
    // -----------------------------------------------------------------
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tipoOrigen", ignore = true)
    @Mapping(target = "valorActual", ignore = true)
    @Mapping(target = "fechaCierre", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    @Mapping(target = "modificadoEn", ignore = true)
    @Mapping(target = "checkIns", ignore = true)
    MetaEntity toEntityFromClinica(MetaClinicaRequestDTO dto);

    // -----------------------------------------------------------------
    // SALIDA: MetaEntity -> MetaResponseDTO
    // -----------------------------------------------------------------
    MetaResponseDTO toResponse(MetaEntity entity);
}