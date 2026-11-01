package upeu.edu.pe.Saludabemente.social.perfil.dto;

import java.util.List;
import upeu.edu.pe.Saludabemente.personal.api.social.SocialMiembroEquipoDto;
import upeu.edu.pe.Saludabemente.social.post.dto.AutorResponse;

public record PerfilCabeceraResponse(AutorResponse autor, String portadaUrl, String presentacion,
        boolean propietario, List<SocialMiembroEquipoDto> equipos) {}
