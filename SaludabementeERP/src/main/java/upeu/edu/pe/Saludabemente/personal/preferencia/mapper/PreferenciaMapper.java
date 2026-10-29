package upeu.edu.pe.Saludabemente.personal.preferencia.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import upeu.edu.pe.Saludabemente.personal.preferencia.dto.request.PreferenciaRequest;
import upeu.edu.pe.Saludabemente.personal.preferencia.dto.response.PreferenciaResponse;
import upeu.edu.pe.Saludabemente.personal.preferencia.entity.PreferenciaComunicacion;

@Mapper(componentModel = "spring")
public interface PreferenciaMapper {

    /**
     * Request → Entity.
     * id y persona los controla el service (el id lo genera Oracle; la persona
     * se resuelve antes de mapear).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "persona", ignore = true)
    PreferenciaComunicacion toEntity(PreferenciaRequest request);

    /**
     * Entity → Response.
     * personaId viene de persona.id; el resto coincide por nombre.
     */
    @Mapping(target = "personaId", source = "persona.id")
    PreferenciaResponse toResponse(PreferenciaComunicacion preferencia);
}