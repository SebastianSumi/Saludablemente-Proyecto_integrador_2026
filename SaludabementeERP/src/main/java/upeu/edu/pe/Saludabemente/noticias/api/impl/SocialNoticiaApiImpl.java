package upeu.edu.pe.Saludabemente.noticias.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.noticias.api.social.*;
import upeu.edu.pe.Saludabemente.noticias.noticia.repository.NoticiaRepository;
import upeu.edu.pe.Saludabemente.noticias.shared.enums.EstadoNoticia;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialNoticiaApiImpl implements SocialNoticiaApi {
    private final NoticiaRepository noticias;

    @Override
    public Optional<NoticiaSnapshotDto> obtenerNoticiaPorId(Long idNoticia) {
        if (idNoticia == null) return Optional.empty();
        return noticias.findById(idNoticia).filter(n -> n.getEstado() == EstadoNoticia.PUBLICADO)
                .map(n -> new NoticiaSnapshotDto(n.getId(), n.getTitulo(), n.getResumen(), n.getImagenUrl(),
                        n.getFechaPublicacion(), "PUBLICADA", Boolean.TRUE.equals(n.getOficial()),
                        n.getUrlCompleta(), n.getIdUsuarioAutor(), n.getIdEquipoAutor()));
    }
}
