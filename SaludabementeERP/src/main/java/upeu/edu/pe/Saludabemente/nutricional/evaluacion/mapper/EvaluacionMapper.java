package upeu.edu.pe.Saludabemente.nutricional.evaluacion.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import upeu.edu.pe.Saludabemente.nutricional.antropometrico.mapper.AntropometricoMapper;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.mapper.BioquimicoMapper;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.dto.request.EvaluacionInitRequest;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.dto.response.EvaluacionResponse;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.dto.response.EvaluacionResumen;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.entity.EvaluacionNutricional;

@Mapper(componentModel = "spring",
        uses = {AntropometricoMapper.class, BioquimicoMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EvaluacionMapper {

    /**
     * Fila 21 de PLANIFICACION: convierte el payload inicial a la cabecera.
     * El snapshot (dni, nombres, edad, sexo, área) lo inyecta el Service
     * DESPUÉS de llamar a este método.
     */
    @Mapping(target = "idPeriodo", ignore = true)   // ← el Service lo setea desde el código resuelto
    @Mapping(target = "idEvaluacion", ignore = true)
    @Mapping(target = "documentoIdentidad", ignore = true)
    @Mapping(target = "nombresCompletos", ignore = true)
    @Mapping(target = "edadAlMomento", ignore = true)
    @Mapping(target = "sexoAlMomento", ignore = true)
    @Mapping(target = "areaTrabajoAlMomento", ignore = true)
    @Mapping(target = "fechaEvaluacion", ignore = true)
    @Mapping(target = "estadoEvaluacion", ignore = true)
    @Mapping(target = "observaciones", ignore = true)
    @Mapping(target = "detalleAntropometrico", ignore = true)
    @Mapping(target = "detalleBioquimico", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "usuarioModificacion", ignore = true)
    EvaluacionNutricional toEntity(EvaluacionInitRequest request);

    /**
     * Fila 22: cabecera + detalles al DTO de respuesta completo.
     */
    EvaluacionResponse toResponse(EvaluacionNutricional entity);

    /**
     * Fila 23: proyección ligera para listados.
     */
    @Mapping(target = "estadoEvaluacion",
            expression = "java(entity.getEstadoEvaluacion().name())")
    EvaluacionResumen toResumen(EvaluacionNutricional entity);


}