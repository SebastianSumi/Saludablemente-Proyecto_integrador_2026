package upeu.edu.pe.Saludabemente.noticias.api;

import upeu.edu.pe.Saludabemente.noticias.api.dto.NoticiaFeedPublicDTO;

import java.util.List;
import java.util.Optional;

public interface NoticiaQueryApi {

    List<NoticiaFeedPublicDTO> obtenerUltimasPublicadas(int limite);

    Optional<NoticiaFeedPublicDTO> obtenerPublicadaPorId(Long idNoticia);

    long contarPublicadasPorCategoria(Long idCategoria);
}