package upeu.edu.pe.Saludabemente.social.comentario.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;
import upeu.edu.pe.Saludabemente.social.post.dto.*;
import upeu.edu.pe.Saludabemente.social.post.service.SocialAccesoService;
import upeu.edu.pe.Saludabemente.social.comentario.entity.*;
import upeu.edu.pe.Saludabemente.social.comentario.repository.*;
import upeu.edu.pe.Saludabemente.social.menciones.service.TextoSocialService;
import upeu.edu.pe.Saludabemente.social.api.dto.*;
import upeu.edu.pe.Saludabemente.social.api.eventos.*;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialUsuarioActualDto;
import upeu.edu.pe.Saludabemente.shared.storage.social.*;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import java.time.*;

@Service @RequiredArgsConstructor @Transactional
public class ComentarioService {
    private final ComentarioRepository comentarios;
    private final ComentarioAdjuntoRepository adjuntos;
    private final SocialAccesoService acceso;
    private final AdjuntoSocialStorageApi storage;
    private final TextoSocialService texto;
    private final ApplicationEventPublisher eventos;
    private final Clock socialClock;
    public Long crear(Long post,CrearComentarioRequest r,SocialUsuarioActualDto u) {
        var p=acceso.bloquear(post);acceso.exigirVisible(p);validarTexto(r.contenido());storage.validarReferencias(post,r.adjuntosUrls());
        Comentario padre=null;
        if(r.idComentarioPadre()!=null) {
            padre=acceso.comentarioVisible(r.idComentarioPadre());
            if(!padre.getIdPost().equals(post) || padre.getIdComentarioPadre()!=null) throw new BusinessRuleException("Padre inválido: solo una raíz del mismo post");
        }
        Comentario c=new Comentario();c.setIdPost(post);c.setIdComentarioPadre(r.idComentarioPadre());c.setIdUsuarioAutor(u.idUsuario());
        c.setContenido(r.contenido());c.setFechaComentario(LocalDateTime.now(socialClock));c=comentarios.saveAndFlush(c);
        int orden=0;if(r.adjuntosUrls()!=null) for(String url:r.adjuntosUrls()) {var a=new ComentarioAdjunto();a.setIdComentario(c.getIdComentario());a.setUrl(url);a.setOrden(++orden);adjuntos.save(a);}
        texto.procesar(TipoContenidoMencion.COMENTARIO,c.getIdComentario(),r.contenido(),u.idUsuario());
        Long destinatario=padre==null?p.getAutorId():padre.getIdUsuarioAutor();
        if(!destinatario.equals(u.idUsuario())) {
            if(padre==null) eventos.publishEvent(new ComentarioEnPostEvent(post,c.getIdComentario(),u.idUsuario(),destinatario,LocalDateTime.now(socialClock)));
            else eventos.publishEvent(new RespuestaAComentarioEvent(post,c.getIdComentario(),padre.getIdComentario(),u.idUsuario(),destinatario,LocalDateTime.now(socialClock)));
        }
        return c.getIdComentario();
    }
    public void editar(Long post,Long id,EditarComentarioRequest r,SocialUsuarioActualDto u) {
        acceso.bloquear(post);var c=acceso.comentarioVisible(id);pertenece(c,post);acceso.comentarioPropio(c,u,false);validarTexto(r.contenido());
        c.setContenido(r.contenido());c.setEditado(true);c.setFechaEdicion(LocalDateTime.now(socialClock));texto.procesar(TipoContenidoMencion.COMENTARIO,id,r.contenido(),u.idUsuario());
    }
    public void eliminar(Long post,Long id,SocialUsuarioActualDto u) {
        acceso.bloquear(post);var c=acceso.comentarioVisible(id);pertenece(c,post);acceso.comentarioPropio(c,u,true);c.setActivo(false);
        if(c.getIdComentarioPadre()==null) comentarios.findByIdComentarioPadre(id).forEach(h -> h.setActivo(false));
    }
    public AdjuntoSocialDto subir(Long post,MultipartFile archivo,SocialUsuarioActualDto u) { acceso.visible(post);return storage.guardar(post,archivo); }
    private void pertenece(Comentario c,Long post) { if(!c.getIdPost().equals(post)) throw new BusinessRuleException("Comentario de otro post"); }
    private void validarTexto(String texto) { if(texto==null || texto.isBlank() || texto.length()>1000) throw new BusinessRuleException("Comentario debe contener 1–1000 caracteres"); }
}
