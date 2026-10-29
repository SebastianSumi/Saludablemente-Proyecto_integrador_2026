package upeu.edu.pe.Saludabemente.social.api.eventos;

import java.time.LocalDateTime;

public record ComentarioEnPostEvent(Long idPost, Long idComentario, Long idUsuarioAutor,
        Long idUsuarioDestinatario, LocalDateTime ocurridoEn) {}
