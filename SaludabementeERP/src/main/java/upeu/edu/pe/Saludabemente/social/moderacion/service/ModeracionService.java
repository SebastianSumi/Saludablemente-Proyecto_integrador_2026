package upeu.edu.pe.Saludabemente.social.moderacion.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import upeu.edu.pe.Saludabemente.social.api.dto.*;
import upeu.edu.pe.Saludabemente.social.post.dto.*;
import upeu.edu.pe.Saludabemente.social.post.service.*;
import upeu.edu.pe.Saludabemente.social.moderacion.entity.PostReporte;
import upeu.edu.pe.Saludabemente.social.moderacion.repository.PostReporteRepository;
import upeu.edu.pe.Saludabemente.social.comentario.repository.ComentarioRepository;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialUsuarioActualDto;
import upeu.edu.pe.Saludabemente.exception.*;
import java.time.*;

@Service @RequiredArgsConstructor @Transactional
public class ModeracionService {
    private final PostReporteRepository reportes;
    private final ComentarioRepository comentarios;
    private final upeu.edu.pe.Saludabemente.social.post.repository.PostRepository posts;
    private final SocialAccesoService acceso;
    private final SocialConsultaService consulta;
    private final Clock socialClock;
    public ReporteResponse reportar(Long post,Long comentario,ReportarRequest r,SocialUsuarioActualDto u) {
        if((post==null)==(comentario==null)) throw new BusinessRuleException("Debe reportar un único target");
        Long targetPost=post!=null?post:acceso.comentarioVisible(comentario).getIdPost();
        acceso.bloquear(targetPost);acceso.visible(targetPost);
        if(comentario!=null) acceso.comentarioVisible(comentario);
        if(post!=null?reportes.existsByIdUsuarioReportaAndIdPost(u.idUsuario(),post):reportes.existsByIdUsuarioReportaAndIdComentario(u.idUsuario(),comentario))
            throw new BusinessRuleException("Ya reportó este contenido");
        if(r.motivo()==null || r.detalle()!=null&&r.detalle().length()>500) throw new BusinessRuleException("Reporte inválido");
        PostReporte rep=new PostReporte();rep.setIdPost(post);rep.setIdComentario(comentario);rep.setIdUsuarioReporta(u.idUsuario());rep.setMotivo(r.motivo());rep.setDetalle(r.detalle());rep.setFechaReporte(LocalDateTime.now(socialClock));return dto(reportes.save(rep));
    }
    @Transactional(readOnly=true) public Page<ReporteResponse> listar(EstadoReporte estado,Pageable pagina,SocialUsuarioActualDto u) {
        acceso.admin(u);return reportes.findByEstadoOrderByFechaReporteAsc(estado,pagina).map(this::dto);
    }
    public ReporteResponse resolver(Long id,ResolverReporteRequest req,SocialUsuarioActualDto u) {
        acceso.admin(u);PostReporte r=reportes.bloquear(id).orElseThrow(()->new ResourceNotFoundException("Reporte no encontrado"));
        if(r.getEstado()==EstadoReporte.ACEPTADO || r.getEstado()==EstadoReporte.RECHAZADO) throw new BusinessRuleException("Reporte ya resuelto");
        if(req.estado()!=EstadoReporte.ACEPTADO && req.estado()!=EstadoReporte.RECHAZADO) throw new BusinessRuleException("Resolución debe ser ACEPTADO o RECHAZADO");
        if(req.comentario()!=null&&req.comentario().length()>500) throw new BusinessRuleException("Comentario demasiado largo");
        r.setEstado(req.estado());r.setFechaRevision(LocalDateTime.now(socialClock));r.setIdUsuarioRevisor(u.idUsuario());r.setComentarioRevision(req.comentario());
        r.setAccionTomada(req.estado()==EstadoReporte.ACEPTADO?"OCULTADO":"NINGUNA");
        if(req.estado()==EstadoReporte.ACEPTADO) {if(r.getIdPost()!=null) ocultarPost(r.getIdPost(),false,u);else ocultarComentario(r.getIdComentario(),false,u);}
        return dto(r);
    }
    public void ocultarPost(Long id,boolean restaurar,SocialUsuarioActualDto u) {
        acceso.admin(u);var previo=acceso.post(id);
        if(previo.getOrigenPostId()!=null) acceso.bloquear(previo.getOrigenPostId());
        var p=acceso.bloquear(id);
        if(restaurar && !Boolean.TRUE.equals(p.getActivo()) && !Boolean.TRUE.equals(p.getOcultoModeracion())) throw new BusinessRuleException("El contenido no fue ocultado por moderación");
        if(restaurar && p.getTipoContenido()==TipoContenidoPost.NOTICIA && !"PUBLICADA".equals(p.getRefEstado())) throw new BusinessRuleException("La noticia no está publicada");
        p.setOcultoModeracion(!restaurar);p.setActivo(restaurar);
        posts.flush();if(p.getOrigenPostId()!=null) acceso.bloquear(p.getOrigenPostId()).setSharesTotal(posts.countByOrigenPostIdAndActivoTrue(p.getOrigenPostId()));
    }
    public void ocultarComentario(Long id,boolean restaurar,SocialUsuarioActualDto u) {
        acceso.admin(u);var c=acceso.comentario(id);acceso.bloquear(c.getIdPost());
        if(!restaurar && !Boolean.TRUE.equals(c.getActivo())) return;
        if(restaurar && !Boolean.TRUE.equals(c.getOcultoModeracion())) throw new BusinessRuleException("El comentario no fue ocultado por moderación");
        if(restaurar) {acceso.visible(c.getIdPost());if(c.getIdComentarioPadre()!=null) acceso.comentarioVisible(c.getIdComentarioPadre());}
        c.setActivo(restaurar);c.setOcultoModeracion(!restaurar);
        if(!restaurar && c.getIdComentarioPadre()==null) comentarios.findByIdComentarioPadre(id).stream().filter(h->Boolean.TRUE.equals(h.getActivo())).forEach(h->{h.setActivo(false);h.setOcultoModeracion(true);});
    }
    private ReporteResponse dto(PostReporte r) {return new ReporteResponse(r.getIdPostReporte(),r.getIdPost(),r.getIdComentario(),consulta.autor(r.getIdUsuarioReporta()),r.getMotivo(),r.getDetalle(),r.getFechaReporte(),r.getEstado(),r.getIdUsuarioRevisor()==null?null:consulta.autor(r.getIdUsuarioRevisor()),r.getFechaRevision(),r.getAccionTomada(),r.getComentarioRevision(),r.getIdPost()!=null?r.getIdPost():acceso.comentario(r.getIdComentario()).getIdPost());}
}
