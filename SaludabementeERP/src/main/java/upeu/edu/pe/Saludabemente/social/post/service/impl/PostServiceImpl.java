package upeu.edu.pe.Saludabemente.social.post.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import upeu.edu.pe.Saludabemente.social.api.SocialApiService;
import upeu.edu.pe.Saludabemente.social.api.dto.*;
import upeu.edu.pe.Saludabemente.social.api.eventos.NuevoPostDeEquipoEvent;
import upeu.edu.pe.Saludabemente.social.post.entity.*;
import upeu.edu.pe.Saludabemente.social.post.repository.*;
import upeu.edu.pe.Saludabemente.social.post.dto.*;
import upeu.edu.pe.Saludabemente.social.post.service.*;
import upeu.edu.pe.Saludabemente.social.menciones.service.TextoSocialService;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialUsuarioActualDto;
import upeu.edu.pe.Saludabemente.personal.api.social.SocialEquipoApi;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import java.time.*;
import java.net.URI;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional
public class PostServiceImpl implements PostService,SocialApiService {
    private final PostRepository posts;
    private final PostImagenRepository imagenes;
    private final SocialAccesoService acceso;
    private final TextoSocialService texto;
    private final SocialEquipoApi equipos;
    private final FijadoService fijados;
    private final ApplicationEventPublisher eventos;
    private final Clock socialClock;

    @Override public Long crear(CrearPostRequest r,SocialUsuarioActualDto u) {
        acceso.equipo(r.idEquipoAutor(),u);validar(r.titulo(),r.resumen(),r.imagenesUrls());
        Post p=base(TipoContenidoPost.MANUAL,null,u.idUsuario());p.setTitulo(r.titulo());p.setResumen(r.resumen());p.setIdEquipoAutor(r.idEquipoAutor());
        p=posts.saveAndFlush(p);guardarImagenes(p,r.imagenesUrls());
        texto.procesar(TipoContenidoMencion.POST,p.getIdPost(),contenido(p),u.idUsuario());
        notificarEquipo(p);return p.getIdPost();
    }
    @Override public void editar(Long id,EditarPostRequest r,SocialUsuarioActualDto u) {
        Post p=acceso.bloquear(id);acceso.exigirVisible(p);acceso.editar(p,u);
        if(p.getTipoContenido()==TipoContenidoPost.MANUAL) validar(r.titulo(),r.resumen(),r.imagenesUrls());
        else if(r.resumen()!=null && r.resumen().length()>500) throw new BusinessRuleException("Texto demasiado largo");
        p.setTitulo(r.titulo());p.setResumen(r.resumen());p.setFechaActualizacion(LocalDateTime.now(socialClock));
        guardarImagenes(p,r.imagenesUrls());texto.procesar(TipoContenidoMencion.POST,id,contenido(p),u.idUsuario());
    }
    @Override public void eliminar(Long id,SocialUsuarioActualDto u) {
        Post original=acceso.post(id);
        if(original.getOrigenPostId()!=null) acceso.bloquear(original.getOrigenPostId());
        Post p=acceso.bloquear(id);acceso.eliminar(p,u);p.setActivo(false);posts.flush();
        if(p.getOrigenPostId()!=null) actualizarShares(p.getOrigenPostId());
    }
    @Override public void sincronizarPostDeActividad(ActividadSocialDto d) {
        if(d==null || d.idActividad()==null || d.idUsuarioCreador()==null) throw new BusinessRuleException("Actividad incompleta");
        Post p=sincronizado(TipoContenidoPost.ACTIVIDAD,d.idActividad(),d.idUsuarioCreador());
        p.setTitulo(d.nombre());p.setResumen(d.descripcion());p.setLugar(d.lugar());p.setFechaEvento(d.fechaActividad());
        p.setHoraInicio(d.horaInicio()==null?null:d.horaInicio().toString());p.setHoraFin(d.horaFin()==null?null:d.horaFin().toString());
        p.setRefEstado(d.estado());p.setImagenUrl(d.logoUrl());p.setCoordinadorNombre(d.nombreCoordinador());p.setIdEquipoAutor(d.idEquipoActividad());
        p.setFechaActualizacion(LocalDateTime.now(socialClock));p=posts.saveAndFlush(p);guardarImagenes(p,d.imagenesUrls());
        texto.procesar(TipoContenidoMencion.POST,p.getIdPost(),contenido(p),p.getAutorId());
    }
    @Override public void sincronizarPostDeNoticia(NoticiaSocialDto d) {
        if(d==null || d.idNoticia()==null || d.idUsuarioAutor()==null) throw new BusinessRuleException("Noticia incompleta");
        if(!d.oficial() || !"PUBLICADA".equals(d.estado())) { ocultarPostDeNoticia(d.idNoticia());return; }
        Post p=sincronizado(TipoContenidoPost.NOTICIA,d.idNoticia(),d.idUsuarioAutor());
        p.setTitulo(d.titulo());p.setResumen(d.resumen());p.setImagenUrl(d.imagenPortadaUrl());p.setEsOficial(true);
        p.setRefEstado(d.estado());p.setIdEquipoAutor(d.idEquipoAutor());
        if(p.getIdPost()==null && d.fechaPublicacion()!=null) p.setFechaPublicacion(d.fechaPublicacion());
        p.setFechaActualizacion(LocalDateTime.now(socialClock));p=posts.saveAndFlush(p);
        guardarImagenes(p,d.imagenPortadaUrl()==null?List.of():List.of(d.imagenPortadaUrl()));
        texto.procesar(TipoContenidoMencion.POST,p.getIdPost(),contenido(p),p.getAutorId());
        if(Boolean.TRUE.equals(p.getActivo())) fijados.autoFijar(p);
    }
    @Override public void ocultarPostDeActividad(Long id) { ocultar(TipoContenidoPost.ACTIVIDAD,id); }
    @Override public void ocultarPostDeNoticia(Long id) { ocultar(TipoContenidoPost.NOTICIA,id); }
    private void ocultar(TipoContenidoPost tipo,Long id) { posts.findByTipoContenidoAndRefId(tipo,id).ifPresent(p -> {p.setActivo(false);p.setRefEstado("NO_PUBLICADA");}); }
    private Post sincronizado(TipoContenidoPost tipo,Long ref,Long autor) {
        Post p=posts.findByTipoContenidoAndRefId(tipo,ref).map(x -> acceso.bloquear(x.getIdPost())).orElseGet(() -> base(tipo,ref,autor));
        p.setAutorId(autor);if(!Boolean.TRUE.equals(p.getOcultoModeracion())) p.setActivo(true);return p;
    }
    private Post base(TipoContenidoPost tipo,Long ref,Long autor) {
        Post p=new Post();p.setTipoContenido(tipo);p.setRefId(ref);p.setAutorId(autor);p.setFechaPublicacion(LocalDateTime.now(socialClock));return p;
    }
    private String contenido(Post p) { return Objects.toString(p.getTitulo(),"")+"\n"+Objects.toString(p.getResumen(),""); }
    private void validar(String titulo,String resumen,List<String> urls) {
        if((titulo==null || titulo.isBlank() || resumen==null || resumen.isBlank()) && (urls==null || urls.isEmpty()))
            throw new BusinessRuleException("Se requiere título y resumen, o una imagen");
        if(titulo!=null && titulo.length()>150 || resumen!=null && resumen.length()>500) throw new BusinessRuleException("Texto demasiado largo");
    }
    private void guardarImagenes(Post p,List<String> urls) {
        imagenes.deleteByIdPost(p.getIdPost());imagenes.flush();
        int orden=0;
        if(urls!=null) for(String url:urls) {
            URI uri;
            try { uri=URI.create(url); } catch(Exception e) { throw new BusinessRuleException("URL de imagen inválida"); }
            if(url.length()>500 || uri.getHost()==null || !("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())))
                throw new BusinessRuleException("URL de imagen inválida");
            PostImagen i=new PostImagen();i.setIdPost(p.getIdPost());i.setUrl(url);i.setOrden(++orden);imagenes.save(i);
        }
        if(p.getTipoContenido()==TipoContenidoPost.MANUAL) p.setImagenUrl(urls==null || urls.isEmpty()?null:urls.getFirst());
    }
    private void notificarEquipo(Post p) {
        if(p.getIdEquipoAutor()!=null) eventos.publishEvent(new NuevoPostDeEquipoEvent(p.getIdPost(),p.getIdEquipoAutor(),p.getAutorId(),
                equipos.miembrosActivosDeEquipo(p.getIdEquipoAutor()).stream().filter(id -> !id.equals(p.getAutorId())).toList(),LocalDateTime.now(socialClock)));
    }
    private void actualizarShares(Long id) { Post p=acceso.bloquear(id);p.setSharesTotal(posts.countByOrigenPostIdAndActivoTrue(id)); }
}
