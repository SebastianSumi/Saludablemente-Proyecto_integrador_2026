package upeu.edu.pe.Saludabemente.social.api.eventos;

import java.time.LocalDateTime;

public record RespuestaAComentarioEvent(Long idPost, Long idComentario, Long idComentarioPadre,
        Long idUsuarioAutor, Long idUsuarioDestinatario, LocalDateTime ocurridoEn) {}
