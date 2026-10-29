package upeu.edu.pe.Saludabemente.noticias.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.noticias.api.NoticiaQueryApi;
import upeu.edu.pe.Saludabemente.noticias.api.dto.NoticiaFeedPublicDTO;
import upeu.edu.pe.Saludabemente.noticias.noticia.repository.NoticiaFeedProjection;
import upeu.edu.pe.Saludabemente.noticias.noticia.repository.NoticiaRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NoticiaQueryApiImpl implements NoticiaQueryApi {

    private final NoticiaRepository noticiaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NoticiaFeedPublicDTO> obtenerUltimasPublicadas(int limite) {
        if (limite <= 0) return List.of();
        return noticiaRepository.findUltimasPublicadas(PageRequest.of(0, limite))
                .stream()
                .map(this::toPublicDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NoticiaFeedPublicDTO> obtenerPublicadaPorId(Long idNoticia) {
        if (idNoticia == null) return Optional.empty();
        return noticiaRepository.findPublicadaPorId(idNoticia).map(this::toPublicDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPublicadasPorCategoria(Long idCategoria) {
        if (idCategoria == null) return 0L;
        return noticiaRepository.countPublicadasPorCategoria(idCategoria);
    }

    private NoticiaFeedPublicDTO toPublicDTO(NoticiaFeedProjection p) {
        return new NoticiaFeedPublicDTO(
                p.getId(),
                p.getTitulo(),
                p.getSubtitulo(),
                p.getNombreCategoria(),
                p.getColorCategoria(),
                p.getImagenUrl(),
                p.getFechaPublicacion()
        );
    }
}