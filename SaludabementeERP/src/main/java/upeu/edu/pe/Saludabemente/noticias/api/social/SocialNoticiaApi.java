package upeu.edu.pe.Saludabemente.noticias.api.social;

import java.util.Optional;

/** Solo noticias PUBLICADO; borrador/archivado/ausente producen Optional.empty. */
public interface SocialNoticiaApi {
    Optional<NoticiaSnapshotDto> obtenerNoticiaPorId(Long idNoticia);
}
