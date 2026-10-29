package upeu.edu.pe.Saludabemente.social.api.eventos;

import java.time.LocalDateTime;
import java.util.List;

public record NuevoPostDeEquipoEvent(Long idPost, Long idEquipo, Long idUsuarioAutor,
        List<Long> idUsuariosDestinatarios, LocalDateTime ocurridoEn) {
    public NuevoPostDeEquipoEvent { idUsuariosDestinatarios = List.copyOf(idUsuariosDestinatarios); }
}
