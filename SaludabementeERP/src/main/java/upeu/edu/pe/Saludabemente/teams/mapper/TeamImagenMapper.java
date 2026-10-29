package upeu.edu.pe.Saludabemente.teams.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.shared.storage.AlmacenamientoImagenService;

@Component
@RequiredArgsConstructor
public class TeamImagenMapper {
    private final AlmacenamientoImagenService imagenes;
    @Named("teamImagenUrl")
    public String url(String key) { return imagenes.construirUrlPublica(key); }
}
