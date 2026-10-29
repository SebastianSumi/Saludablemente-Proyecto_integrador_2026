package upeu.edu.pe.Saludabemente.seguridad.api.social;

import java.util.List;

/** Identidad técnica de cuenta activa; idPersona nunca se deduce de idUsuario. */
public record SocialIdentidadDto(Long idUsuario, Long idPersona, String email, List<String> roles) {
    public SocialIdentidadDto {
        roles = List.copyOf(roles);
    }
}
