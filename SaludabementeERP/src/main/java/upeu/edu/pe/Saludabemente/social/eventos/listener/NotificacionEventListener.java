package upeu.edu.pe.Saludabemente.social.eventos.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.*;
import upeu.edu.pe.Saludabemente.social.api.eventos.*;
import upeu.edu.pe.Saludabemente.social.api.dto.TipoContenidoMencion;
import upeu.edu.pe.Saludabemente.social.comentario.repository.ComentarioRepository;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialIdentidadApi;
import upeu.edu.pe.Saludabemente.notificaciones.api.social.*;
import java.util.*;

@Component @RequiredArgsConstructor @Slf4j
public class NotificacionEventListener {
    private final SocialIdentidadApi identidades;
    private final SocialNotificacionApi notificaciones;
    private final ComentarioRepository comentarios;
    @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)
    public void comentario(ComentarioEnPostEvent e) {enviar(TipoAvisoSocial.SOCIAL_COMENTARIO_POST,e.idUsuarioAutor(),e.idUsuarioDestinatario(),e.idPost(),e.idComentario(),null);}
    @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)
    public void respuesta(RespuestaAComentarioEvent e) {enviar(TipoAvisoSocial.SOCIAL_RESPUESTA_COMENTARIO,e.idUsuarioAutor(),e.idUsuarioDestinatario(),e.idPost(),e.idComentario(),null);}
    @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)
    public void reaccion(ReaccionEnPostEvent e) {enviar(TipoAvisoSocial.SOCIAL_REACCION_POST,e.idUsuarioAutor(),e.idUsuarioDestinatario(),e.idPost(),null,null);}
    @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)
    public void mencion(MencionEvent e) {
        if(e.tipoContenido()==TipoContenidoMencion.POST) enviar(TipoAvisoSocial.SOCIAL_MENCION,e.idUsuarioAutor(),e.idUsuarioDestinatario(),e.idContenido(),null,null);
        else comentarios.findById(e.idContenido()).ifPresent(c->enviar(TipoAvisoSocial.SOCIAL_MENCION,e.idUsuarioAutor(),e.idUsuarioDestinatario(),c.getIdPost(),c.getIdComentario(),null));
    }
    @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)
    public void equipo(NuevoPostDeEquipoEvent e) {new HashSet<>(e.idUsuariosDestinatarios()).forEach(id->enviar(TipoAvisoSocial.SOCIAL_NUEVO_POST_EQUIPO,e.idUsuarioAutor(),id,e.idPost(),null,e.idEquipo()));}
    private void enviar(TipoAvisoSocial tipo,Long autor,Long usuario,Long post,Long comentario,Long equipo) {
        if(usuario==null||usuario.equals(autor)) return;
        try {identidades.obtenerIdentidadActiva(usuario).ifPresent(i->notificaciones.enviar(new SocialAvisoDto(UUID.randomUUID(),tipo,i.idPersona(),post,comentario,equipo)));}
        catch(RuntimeException error) {log.error("No se pudo publicar aviso social tipo={} post={} usuario={}; requiere recuperación operativa",tipo,post,usuario,error);}
    }
}
