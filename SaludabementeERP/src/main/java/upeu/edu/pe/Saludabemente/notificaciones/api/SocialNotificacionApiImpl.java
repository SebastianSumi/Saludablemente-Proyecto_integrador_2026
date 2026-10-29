package upeu.edu.pe.Saludabemente.notificaciones.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.notificaciones.api.social.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SocialNotificacionApiImpl implements SocialNotificacionApi {
    private final NotificacionApi notificaciones;

    @Override
    public void enviar(SocialAvisoDto aviso) {
        if (aviso == null || aviso.idEvento() == null || aviso.tipo() == null
                || aviso.idPersonaDestinatario() == null || aviso.idPersonaDestinatario() <= 0
                || aviso.idPost() == null || aviso.idPost() <= 0)
            throw new BusinessRuleException("Aviso social incompleto");
        if ((aviso.tipo() == TipoAvisoSocial.SOCIAL_COMENTARIO_POST
                || aviso.tipo() == TipoAvisoSocial.SOCIAL_RESPUESTA_COMENTARIO)
                && (aviso.idComentario() == null || aviso.idComentario() <= 0))
            throw new BusinessRuleException("Comentario requerido para el aviso");
        if (aviso.tipo() == TipoAvisoSocial.SOCIAL_NUEVO_POST_EQUIPO
                && (aviso.idEquipo() == null || aviso.idEquipo() <= 0))
            throw new BusinessRuleException("Equipo requerido para el aviso");
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("idEvento", aviso.idEvento().toString());
        variables.put("idPost", aviso.idPost());
        if (aviso.idComentario() != null) variables.put("idComentario", aviso.idComentario());
        if (aviso.idEquipo() != null) variables.put("idEquipo", aviso.idEquipo());
        notificaciones.enviar(aviso.idPersonaDestinatario(), aviso.tipo().name(), "social",
                aviso.idPost(), Map.copyOf(variables), null);
    }
}
