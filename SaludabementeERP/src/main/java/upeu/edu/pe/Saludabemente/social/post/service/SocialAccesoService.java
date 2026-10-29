package upeu.edu.pe.Saludabemente.social.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import upeu.edu.pe.Saludabemente.exception.*;
import upeu.edu.pe.Saludabemente.personal.api.social.SocialEquipoApi;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialUsuarioActualDto;
import upeu.edu.pe.Saludabemente.social.api.dto.TipoContenidoPost;
import upeu.edu.pe.Saludabemente.social.post.entity.Post;
import upeu.edu.pe.Saludabemente.social.post.repository.PostRepository;
import upeu.edu.pe.Saludabemente.social.comentario.entity.Comentario;
import upeu.edu.pe.Saludabemente.social.comentario.repository.ComentarioRepository;

@Service @RequiredArgsConstructor
public class SocialAccesoService {
    private final PostRepository posts;
    private final ComentarioRepository comentarios;
    private final SocialEquipoApi equipos;

    public Post post(Long id) { return posts.findById(id).orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada")); }
    public Post bloquear(Long id) { return posts.bloquear(id).orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada")); }
    public Post visible(Long id) { Post p=post(id); exigirVisible(p); return p; }
    public void exigirVisible(Post p) {
        if (!Boolean.TRUE.equals(p.getActivo())) throw new ResourceNotFoundException("Publicación no disponible");
        if (p.getOrigenPostId()!=null && !posts.findById(p.getOrigenPostId()).map(o -> Boolean.TRUE.equals(o.getActivo())).orElse(false))
            throw new ResourceNotFoundException("Publicación original no disponible");
    }
    public Comentario comentario(Long id) {
        return comentarios.findById(id).orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado"));
    }
    public Comentario comentarioVisible(Long id) {
        Comentario c=comentario(id); visible(c.getIdPost());
        if (!Boolean.TRUE.equals(c.getActivo())) throw new ResourceNotFoundException("Comentario no disponible");
        return c;
    }
    public void admin(SocialUsuarioActualDto u) { if (!u.esAdministrador()) throw new AccessDeniedException("Se requiere administrador"); }
    public void equipo(Long equipo, SocialUsuarioActualDto u) {
        if(equipo!=null && !equipos.esMiembroDeEquipo(u.idUsuario(),equipo)) throw new AccessDeniedException("No pertenece al equipo");
    }
    public boolean editable(Post p, SocialUsuarioActualDto u) {
        return manual(p) && (u.esAdministrador() || p.getAutorId().equals(u.idUsuario())
                || (p.getIdEquipoAutor()!=null && equipos.esAdminDeEquipo(u.idUsuario(),p.getIdEquipoAutor())));
    }
    public boolean eliminable(Post p,SocialUsuarioActualDto u) {
        return manual(p) && (u.esAdministrador() || p.getAutorId().equals(u.idUsuario()));
    }
    public boolean manual(Post p) { return p.getTipoContenido()==TipoContenidoPost.MANUAL || p.getTipoContenido()==TipoContenidoPost.SHARE; }
    public void editar(Post p,SocialUsuarioActualDto u) { if(!editable(p,u)) throw new AccessDeniedException("No puede editar esta publicación"); }
    public void eliminar(Post p,SocialUsuarioActualDto u) { if(!eliminable(p,u)) throw new AccessDeniedException("No puede eliminar esta publicación"); }
    public void comentarioPropio(Comentario c,SocialUsuarioActualDto u,boolean borrar) {
        if(!c.getIdUsuarioAutor().equals(u.idUsuario()) && !(borrar && u.esAdministrador()))
            throw new AccessDeniedException("No puede modificar este comentario");
    }
}
