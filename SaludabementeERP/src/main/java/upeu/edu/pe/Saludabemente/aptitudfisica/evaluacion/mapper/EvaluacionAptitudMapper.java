package upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import upeu.edu.pe.Saludabemente.aptitudfisica.cardio.mapper.CardioMapper;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.dto.response.EvaluacionAptitudResponse;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.dto.resumen.EvaluacionResumen;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.entity.EvaluacionAptitud;
import upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.mapper.FuerzaMapper;
import upeu.edu.pe.Saludabemente.personal.persona.dto.PersonaSnapshotClinicoDTO;

import java.time.LocalDate;

/**
 * Mapeo cabecera ↔ DTOs. El snapshot se inyecta desde PersonaSnapshotClinicoDTO
 * al crear la entidad; el resto es conversión directa.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {FuerzaMapper.class, CardioMapper.class}
)
public interface EvaluacionAptitudMapper {

    /**
     * Construye la cabecera con snapshot inmutable desde el DTO de Personal.
     * Los 3 parámetros se mapean explícitamente porque los nombres no coinciden.
     */
    @Mapping(target = "id",                        ignore = true)
    @Mapping(target = "idPersona",                 source = "snapshot.idPersona")
    @Mapping(target = "idPeriodo",                 source = "idPeriodo")
    @Mapping(target = "documentoIdentidadAlMomento", source = "snapshot.documentoIdentidad")
    @Mapping(target = "nombresCompletosAlMomento", source = "snapshot.nombresCompletos")
    @Mapping(target = "edadAlMomento",             source = "snapshot.edadAlMomento")
    @Mapping(target = "sexoAlMomento",             source = "snapshot.sexo")
    @Mapping(target = "areaTrabajoAlMomento",      source = "snapshot.areaTrabajo")
    @Mapping(target = "fechaEvaluacion",           source = "fechaEvaluacion")
    @Mapping(target = "estadoEvaluacion",          ignore = true)
    @Mapping(target = "dictamenGlobal",            ignore = true)
    @Mapping(target = "observacionesRestricciones", ignore = true)
    @Mapping(target = "fitnessScore",              ignore = true)
    @Mapping(target = "detalleFuerza",             ignore = true)
    @Mapping(target = "detalleCardio",             ignore = true)
    EvaluacionAptitud toEntity(PersonaSnapshotClinicoDTO snapshot,
                               Long idPeriodo,
                               LocalDate fechaEvaluacion);

    @Mapping(target = "estadoEvaluacion",
            expression = "java(enumToString(entity.getEstadoEvaluacion()))")
    @Mapping(target = "dictamenGlobal",
            expression = "java(enumToString(entity.getDictamenGlobal()))")
    EvaluacionAptitudResponse toResponse(EvaluacionAptitud entity);

    @Mapping(target = "estadoEvaluacion",
            expression = "java(enumToString(entity.getEstadoEvaluacion()))")
    @Mapping(target = "dictamenGlobal",
            expression = "java(enumToString(entity.getDictamenGlobal()))")
    EvaluacionResumen toResumen(EvaluacionAptitud entity);

    /** Helper: enum → String, null-safe. */
    @Named("enumToString")
    default String enumToString(Enum<?> e) {
        return e == null ? null : e.name();
    }
}