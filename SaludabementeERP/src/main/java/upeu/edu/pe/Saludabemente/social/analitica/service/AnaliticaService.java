package upeu.edu.pe.Saludabemente.social.analitica.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.social.post.service.*;
import upeu.edu.pe.Saludabemente.social.post.repository.*;
import upeu.edu.pe.Saludabemente.social.comentario.repository.ComentarioRepository;
import upeu.edu.pe.Saludabemente.social.reaccion.repository.ReaccionPostRepository;
import upeu.edu.pe.Saludabemente.social.reaccion.repository.ReaccionComentarioRepository;
import upeu.edu.pe.Saludabemente.social.post.dto.*;
import upeu.edu.pe.Saludabemente.social.api.dto.TipoContenidoPost;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialUsuarioActualDto;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class AnaliticaService {
    private final PostRepository posts;
    private final ComentarioRepository comentarios;
    private final ReaccionPostRepository reacciones;
    private final ReaccionComentarioRepository reaccionesComentario;
    private final SocialAccesoService acceso;
    private final SocialConsultaService consulta;
    private final Clock socialClock;

    public MetricaResponse metricas(LocalDate desde,LocalDate hasta,SocialUsuarioActualDto u) {
        acceso.admin(u);if(desde==null||hasta==null||hasta.isBefore(desde)||hasta.isAfter(desde.plusYears(1))) throw new BusinessRuleException("Rango inválido, máximo un año");
        var visibles=posts.findAll(consulta.visibles());var porId=visibles.stream().collect(Collectors.toMap(p->p.getIdPost(),p->p));
        var pub=visibles.stream().filter(p->enRango(p.getFechaPublicacion(),desde,hasta)).toList();
        var com=comentarios.findAll().stream().filter(c->Boolean.TRUE.equals(c.getActivo())&&porId.containsKey(c.getIdPost())&&enRango(c.getFechaComentario(),desde,hasta)).toList();
        var rea=reacciones.findAll().stream().filter(r->porId.containsKey(r.getIdPost())&&enRango(r.getFechaReaccion(),desde,hasta)).toList();
        Set<Long> comentariosActivos=comentarios.findAll().stream().filter(c->Boolean.TRUE.equals(c.getActivo())&&porId.containsKey(c.getIdPost())).map(c->c.getIdComentario()).collect(Collectors.toSet());
        var rc=reaccionesComentario.findAll().stream().filter(r->comentariosActivos.contains(r.getIdComentario())&&enRango(r.getFechaReaccion(),desde,hasta)).toList();
        Map<Long,Long> actividad=new HashMap<>();pub.forEach(p->actividad.merge(p.getAutorId(),1L,Long::sum));com.forEach(c->actividad.merge(c.getIdUsuarioAutor(),1L,Long::sum));rea.forEach(r->actividad.merge(r.getIdUsuario(),1L,Long::sum));rc.forEach(r->actividad.merge(r.getIdUsuario(),1L,Long::sum));
        Map<Long,Long> cuentaCom=com.stream().collect(Collectors.groupingBy(c->c.getIdPost(),Collectors.counting()));
        Map<Long,Long> cuentaRea=rea.stream().collect(Collectors.groupingBy(r->r.getIdPost(),Collectors.counting()));
        var topCom=cuentaCom.entrySet().stream().sorted(Map.Entry.<Long,Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey())).limit(10).map(e->new TopPost(e.getKey(),porId.get(e.getKey()).getTitulo(),e.getValue())).toList();
        var topRea=cuentaRea.entrySet().stream().sorted(Map.Entry.<Long,Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey())).limit(10).map(e->new TopPost(e.getKey(),porId.get(e.getKey()).getTitulo(),e.getValue())).toList();
        var topUsu=actividad.entrySet().stream().sorted(Map.Entry.<Long,Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey())).limit(10).map(e->new TopUsuario(e.getKey(),consulta.autor(e.getKey()),e.getValue())).toList();
        List<VolumenDiario> diario=new ArrayList<>();for(LocalDate d=desde;!d.isAfter(hasta);d=d.plusDays(1)) {LocalDate fecha=d;diario.add(new VolumenDiario(d,pub.stream().filter(p->p.getFechaPublicacion().toLocalDate().equals(fecha)).count(),com.stream().filter(c->c.getFechaComentario().toLocalDate().equals(fecha)).count(),rea.stream().filter(r->r.getFechaReaccion().toLocalDate().equals(fecha)).count()+rc.stream().filter(r->r.getFechaReaccion().toLocalDate().equals(fecha)).count()));}
        long shares=pub.stream().filter(p->p.getTipoContenido()==TipoContenidoPost.SHARE).count();
        return new MetricaResponse(pub.size(),com.size(),rea.size()+rc.size(),shares,actividad.size(),pub.isEmpty()?0.0:(double)(com.size()+rea.size()+rc.size()+shares)/pub.size(),topCom,topRea,topUsu,consulta.tendenciasRango(desde.atStartOfDay(),hasta.plusDays(1).atStartOfDay().minusNanos(1),10),diario);
    }
    private boolean enRango(LocalDateTime fecha,LocalDate desde,LocalDate hasta) {return fecha!=null&&!fecha.toLocalDate().isBefore(desde)&&!fecha.toLocalDate().isAfter(hasta);}
}
