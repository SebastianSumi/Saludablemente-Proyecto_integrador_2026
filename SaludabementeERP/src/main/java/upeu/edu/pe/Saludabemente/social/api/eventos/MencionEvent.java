package upeu.edu.pe.Saludabemente.social.api.eventos;

import java.time.LocalDateTime;
import upeu.edu.pe.Saludabemente.social.api.dto.TipoContenidoMencion;

public record MencionEvent(Long idMencion, TipoContenidoMencion tipoContenido, Long idContenido,
        Long idUsuarioAutor, Long idUsuarioDestinatario, LocalDateTime ocurridoEn) {}
