package upeu.edu.pe.Saludabemente.social.api.eventos;

import java.time.LocalDateTime;
import upeu.edu.pe.Saludabemente.social.api.dto.TipoReaccion;

public record ReaccionEnPostEvent(Long idPost, Long idUsuarioAutor, Long idUsuarioDestinatario,
        TipoReaccion tipo, LocalDateTime ocurridoEn) {}
