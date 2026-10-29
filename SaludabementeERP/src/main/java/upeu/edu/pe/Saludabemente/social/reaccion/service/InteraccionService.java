package upeu.edu.pe.Saludabemente.social.reaccion.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import upeu.edu.pe.Saludabemente.social.api.dto.*;
import upeu.edu.pe.Saludabemente.social.api.eventos.*;
import upeu.edu.pe.Saludabemente.social.post.entity.*;
import upeu.edu.pe.Saludabemente.social.post.repository.*;
import upeu.edu.pe.Saludabemente.social.post.dto.*;
import upeu.edu.pe.Saludabemente.social.post.service.SocialAccesoService;
import upeu.edu.pe.Saludabemente.social.reaccion.entity.*;
import upeu.edu.pe.Saludabemente.social.reaccion.repository.*;
import upeu.edu.pe.Saludabemente.social.menciones.service.TextoSocialService;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialUsuarioActualDto;
import upeu.edu.pe.Saludabemente.personal.api.social.SocialEquipoApi;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import java.time.*;

@Service @RequiredArgsConstructor @Transactional
public class InteraccionService {
    private final PostRepository posts;
    private final PostGuardadoRepository guardados;
    private final PostVistaRepository vistas;
    private final ReaccionPostRepository reacciones;
    private final ReaccionComentarioRepository reaccionesComentario;
    private final SocialAccesoService acceso;
    private final SocialEquipoApi equipos;
    private final TextoSocialService texto;
    private final ApplicationEventPublisher eventos;
    private final Clock socialClock;

    public void reaccionar(Long id,TipoReaccion tipo,SocialUsuarioActualDto u) {
        Post p=acceso.bloquear(id);acceso.exigirVisible(p);ReaccionPostId pk=new ReaccionPostId(id,u.idUsuario());
        if(tipo==null) { reacciones.deleteById(pk);return; }
        ReaccionPost r=reacciones.findById(pk).orElseGet(ReaccionPost::new);
        boolean cambio=r.getTipo()!=tipo;r.setIdPost(id);r.setIdUsuario(u.idUsuario());r.setTipo(tipo);r.setFechaReaccion(LocalDateTime.now(socialClock));reacciones.save(r);
        if(cambio && !p.getAutorId().equals(u.idUsuario())) eventos.publishEvent(new ReaccionEnPostEvent(id,u.idUsuario(),p.getAutorId(),tipo,LocalDateTime.now(socialClock)));
    }
    public void reaccionarComentario(Long id,TipoReaccion tipo,SocialUsuarioActualDto u) {
        var c=acceso.comentarioVisible(id);acceso.bloquear(c.getIdPost());c=acceso.comentarioVisible(id);
        var pk=new ReaccionComentarioId(id,u.idUsuario());
        if(tipo==null) { reaccionesComentario.deleteById(pk);return; }
        var r=reaccionesComentario.findById(pk).orElseGet(ReaccionComentario::new);
        r.setIdComentario(id);r.setIdUsuario(u.idUsuario());r.setTipo(tipo);r.setFechaReaccion(LocalDateTime.now(socialClock));reaccionesComentario.save(r);
    }
    public void guardar(Long id,boolean guardar,SocialUsuarioActualDto u) {
        Post p=acceso.bloquear(id);acceso.exigirVisible(p);var pk=new PostGuardadoId(id,u.idUsuario());
        if(!guardar) { guardados.deleteById(pk);return; }
        if(!guardados.existsById(pk)) { var g=new PostGuardado();g.setIdPost(id);g.setIdUsuario(u.idUsuario());g.setFechaGuardado(LocalDateTime.now(socialClock));guardados.save(g); }
    }
    public void registrarVista(Long id,SocialUsuarioActualDto u) {
        Post p=acceso.bloquear(id);acceso.exigirVisible(p);
        if(u.esAdministrador() || p.getAutorId().equals(u.idUsuario())) return;
        var v=vistas.findByIdPostAndIdUsuario(id,u.idUsuario());
        if(v.isPresent()) { v.get().setFechaUltimaVista(LocalDateTime.now(socialClock));return; }
        var nueva=new PostVista();nueva.setIdPost(id);nueva.setIdUsuario(u.idUsuario());
        nueva.setFechaPrimeraVista(LocalDateTime.now(socialClock));nueva.setFechaUltimaVista(LocalDateTime.now(socialClock));vistas.save(nueva);p.setAlcanceTotal(p.getAlcanceTotal()+1);
    }
    public ShareResponse compartir(Long id,CompartirRequest r,SocialUsuarioActualDto u) {
        acceso.equipo(r.idEquipoAutor(),u);Post inicial=acceso.visible(id);Long root=inicial.getOrigenPostId()==null?id:inicial.getOrigenPostId();
        Post original=acceso.bloquear(root);acceso.exigirVisible(original);
        if(r.contenido()!=null && r.contenido().length()>500) throw new BusinessRuleException("Máximo 500 caracteres");
        Post p=posts.findByAutorIdAndOrigenPostId(u.idUsuario(),root).orElseGet(Post::new);
        if(Boolean.TRUE.equals(p.getOcultoModeracion())) throw new BusinessRuleException("El share está oculto por moderación");
        if(p.getIdPost()!=null && Boolean.TRUE.equals(p.getActivo())) throw new BusinessRuleException("Ya compartió esta publicación");
        p.setTipoContenido(TipoContenidoPost.SHARE);p.setOrigenPostId(root);p.setAutorId(u.idUsuario());p.setIdEquipoAutor(r.idEquipoAutor());
        p.setTitulo(null);p.setResumen(r.contenido());p.setActivo(true);p.setFechaPublicacion(LocalDateTime.now(socialClock));
        p=posts.saveAndFlush(p);original.setSharesTotal(posts.countByOrigenPostIdAndActivoTrue(root));
        texto.procesar(TipoContenidoMencion.POST,p.getIdPost(),r.contenido(),u.idUsuario());
        if(p.getIdEquipoAutor()!=null) eventos.publishEvent(new NuevoPostDeEquipoEvent(p.getIdPost(),p.getIdEquipoAutor(),u.idUsuario(),
                equipos.miembrosActivosDeEquipo(p.getIdEquipoAutor()).stream().filter(x -> !x.equals(u.idUsuario())).toList(),LocalDateTime.now(socialClock)));
        return new ShareResponse(p.getIdPost(),p.getFechaPublicacion());
    }
}
