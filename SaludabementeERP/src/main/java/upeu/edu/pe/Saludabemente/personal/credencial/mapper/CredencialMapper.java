package upeu.edu.pe.Saludabemente.personal.credencial.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import upeu.edu.pe.Saludabemente.personal.credencial.dto.response.CredencialResponse;
import upeu.edu.pe.Saludabemente.personal.credencial.entity.CredencialPrograma;

@Mapper(componentModel = "spring")
public interface CredencialMapper {

    /**
     * Entity → Response sin el qrBase64.
     * El qrBase64 se rellena en el service solo si el cliente lo pide.
     */
    @Mapping(target = "personaId", source = "persona.id")
    @Mapping(target = "qrBase64", ignore = true)
    CredencialResponse toResponse(CredencialPrograma credencial);
}