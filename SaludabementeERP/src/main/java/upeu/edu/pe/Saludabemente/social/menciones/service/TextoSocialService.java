package upeu.edu.pe.Saludabemente.social.menciones.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.social.api.dto.*;
import upeu.edu.pe.Saludabemente.social.api.eventos.MencionEvent;
import upeu.edu.pe.Saludabemente.social.post.entity.PostHashtag;
import upeu.edu.pe.Saludabemente.social.post.repository.PostHashtagRepository;
import upeu.edu.pe.Saludabemente.social.comentario.entity.ComentarioHashtag;
import upeu.edu.pe.Saludabemente.social.comentario.repository.ComentarioHashtagRepository;
import upeu.edu.pe.Saludabemente.social.menciones.entity.Mencion;
import upeu.edu.pe.Saludabemente.social.menciones.repository.MencionRepository;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialUsuarioApi;
import upeu.edu.pe.Saludabemente.actividades.api.social.SocialActividadApi;
import upeu.edu.pe.Saludabemente.personal.api.social.SocialEquipoApi;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import java.time.*;
import java.util.*;
import java.util.regex.*;

@Service @RequiredArgsConstructor @Transactional
public class TextoSocialService {
    private static final Pattern TAG=Pattern.compile("(?<![\\p{L}\\p{N}_])#([a-z0-9_áéíóúñ]{2,50})(?![\\p{L}\\p{N}_])",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
    private static final Pattern MENCION=Pattern.compile("@(actividad|equipo)\\{([0-9]+)\\}|@([a-z0-9]+(?:\\.[a-z0-9]+)*)(?![a-z0-9._])",Pattern.CASE_INSENSITIVE);
    private final PostHashtagRepository postTags;
    private final ComentarioHashtagRepository comentarioTags;
    private final MencionRepository menciones;
    private final SocialUsuarioApi usuarios;
    private final SocialActividadApi actividades;
    private final SocialEquipoApi equipos;
    private final ApplicationEventPublisher eventos;
    private final Clock socialClock;

    public void procesar(TipoContenidoMencion tipo,Long id,String texto,Long autor) {
        String contenido=texto==null?"":texto;
        if(tipo==TipoContenidoMencion.POST) { postTags.deleteByIdPost(id); postTags.flush(); }
        else { comentarioTags.deleteByIdComentario(id); comentarioTags.flush(); }
        Set<String> tags=new LinkedHashSet<>(); Matcher hashtag=TAG.matcher(contenido);
        while(hashtag.find()) tags.add(hashtag.group(1).toLowerCase(Locale.ROOT));
        for(String tag:tags) {
            if(tipo==TipoContenidoMencion.POST) { PostHashtag h=new PostHashtag();h.setIdPost(id);h.setTag(tag);postTags.save(h); }
            else { ComentarioHashtag h=new ComentarioHashtag();h.setIdComentario(id);h.setTag(tag);comentarioTags.save(h); }
        }
        menciones.deleteByTipoContenidoAndIdContenido(tipo,id); menciones.flush();
        Matcher matcher=MENCION.matcher(contenido); Set<Long> notificados=new HashSet<>();
        while(matcher.find()) {
            TipoMencion tm; Long ref;
            try {
                if(matcher.group(3)!=null) {
                    var u=usuarios.obtenerPorUsername(matcher.group(3).toLowerCase(Locale.ROOT));
                    if(u.isEmpty() || !Boolean.TRUE.equals(u.get().activo())) continue;
                    tm=TipoMencion.USUARIO;ref=u.get().idUsuario();
                } else {
                    ref=Long.valueOf(matcher.group(2)); if(ref<=0) continue;
                    if("actividad".equalsIgnoreCase(matcher.group(1))) {
                        tm=TipoMencion.ACTIVIDAD;if(actividades.obtenerActividadPorId(ref).isEmpty()) continue;
                    } else { tm=TipoMencion.EQUIPO;if(!equipos.obtenerPorId(ref).activo()) continue; }
                }
            } catch(ResourceNotFoundException|NumberFormatException ausente) { continue; }
            Mencion m=new Mencion();m.setTipoContenido(tipo);m.setIdContenido(id);m.setTipoMencion(tm);
            m.setRefId(ref);m.setOffsetInicio(matcher.start());m.setOffsetFin(matcher.end());m=menciones.save(m);
            if(tm==TipoMencion.USUARIO && !ref.equals(autor) && notificados.add(ref))
                eventos.publishEvent(new MencionEvent(m.getIdMencion(),tipo,id,autor,ref,LocalDateTime.now(socialClock)));
        }
    }
}
