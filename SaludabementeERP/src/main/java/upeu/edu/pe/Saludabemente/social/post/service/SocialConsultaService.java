package upeu.edu.pe.Saludabemente.social.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;
import upeu.edu.pe.Saludabemente.social.api.dto.*;
import upeu.edu.pe.Saludabemente.social.post.entity.*;
import upeu.edu.pe.Saludabemente.social.post.repository.*;
import upeu.edu.pe.Saludabemente.social.post.dto.*;
import upeu.edu.pe.Saludabemente.social.comentario.entity.*;
import upeu.edu.pe.Saludabemente.social.comentario.repository.*;
import upeu.edu.pe.Saludabemente.social.reaccion.repository.*;
import upeu.edu.pe.Saludabemente.social.reaccion.entity.*;
import upeu.edu.pe.Saludabemente.social.menciones.repository.MencionRepository;
import upeu.edu.pe.Saludabemente.social.integracion.client.SocialUsuarioClientService;
import upeu.edu.pe.Saludabemente.seguridad.api.social.*;
import upeu.edu.pe.Saludabemente.personal.api.social.*;
import upeu.edu.pe.Saludabemente.actividades.api.social.SocialActividadApi;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class SocialConsultaService {
    private final PostRepository posts;
    private final PostImagenRepository imagenes;
    private final PostHashtagRepository postTags;
    private final PostGuardadoRepository guardados;
    private final ComentarioRepository comentarios;
    private final ComentarioAdjuntoRepository adjuntos;
    private final ComentarioHashtagRepository comentarioTags;
    private final ReaccionPostRepository reacciones;
    private final ReaccionComentarioRepository reaccionesComentario;
    private final MencionRepository menciones;
    private final SocialAccesoService acceso;
    private final FijadoService fijados;
    private final SocialUsuarioClientService autores;
    private final SocialUsuarioApi usuarios;
    private final SocialEquipoApi equipos;
    private final SocialActividadApi actividades;
    private final Clock socialClock;

    public Pageable pagina(int page,int size,Sort sort) {
        if(page<0 || size<1 || size>50) throw new BusinessRuleException("Página inválida; tamaño permitido 1–50");
        return PageRequest.of(page,size,sort);
    }
    public Pageable pagina(int page,int size) { return pagina(page,size,Sort.by(Sort.Direction.DESC,"fechaPublicacion","idPost")); }
    public Specification<Post> visibles() {
        return (p,q,b) -> visible(p,q,b);
    }
    private Predicate visible(Root<Post> p,CommonAbstractCriteria q,CriteriaBuilder b) {
        Subquery<Long> original=q.subquery(Long.class);Root<Post> o=original.from(Post.class);
        original.select(o.get("idPost")).where(b.equal(o.get("idPost"),p.get("origenPostId")),b.isTrue(o.get("activo")));
        return b.and(b.isTrue(p.get("activo")),b.or(b.isNull(p.get("origenPostId")),b.exists(original)));
    }
    public MuroResponse muro(String tab,ScopeFijado scope,Long scopeId,String tag,Pageable page,SocialUsuarioActualDto u) {
        Specification<Post> filtro=visibles();
        if(scope==ScopeFijado.EQUIPO) filtro=filtro.and((p,q,b)->b.equal(p.get("idEquipoAutor"),scopeId));
        if(scope==ScopeFijado.ACTIVIDAD) filtro=filtro.and((p,q,b)->b.and(b.equal(p.get("tipoContenido"),TipoContenidoPost.ACTIVIDAD),b.equal(p.get("refId"),scopeId)));
        if(tag!=null) filtro=filtro.and(conTag(tag));
        switch(tab) {
            case "PARA_TI","RECIENTES" -> { }
            case "MIS_EQUIPOS" -> { var ids=equipos.equiposDeUsuario(u.idUsuario()).stream().map(SocialMiembroEquipoDto::idEquipo).toList(); filtro=filtro.and((p,q,b)->ids.isEmpty()?b.disjunction():p.get("idEquipoAutor").in(ids)); }
            case "SOLO_ACTIVIDADES" -> filtro=filtro.and((p,q,b)->b.equal(p.get("tipoContenido"),TipoContenidoPost.ACTIVIDAD));
            case "SOLO_NOTICIAS" -> filtro=filtro.and((p,q,b)->b.equal(p.get("tipoContenido"),TipoContenidoPost.NOTICIA));
            case "MIS_PUBLICACIONES" -> filtro=filtro.and((p,q,b)->b.equal(p.get("autorId"),u.idUsuario()));
            case "MIS_GUARDADOS" -> filtro=filtro.and(guardadoPor(u.idUsuario()));
            default -> throw new BusinessRuleException("Tab de muro inválida");
        }
        List<Long> pinIds=tag!=null?List.of():fijados.activos(scope,scopeId).stream().map(PostFijado::getIdPost).toList();
        List<Post> pinned=pinIds.isEmpty()?List.of():posts.findAll(filtro.and((p,q,b)->p.get("idPost").in(pinIds)));
        Map<Long,Post> porId=pinned.stream().collect(Collectors.toMap(Post::getIdPost,p->p));
        pinned=pinIds.stream().map(porId::get).filter(Objects::nonNull).toList();
        Specification<Post> normales=pinIds.isEmpty()?filtro:filtro.and((p,q,b)->b.not(p.get("idPost").in(pinIds)));
        Page<Post> resultado=posts.findAll(normales,page);
        return new MuroResponse(resumenes(pinned,u),mapear(resultado,u));
    }
    private Specification<Post> conTag(String tag) {
        return (p,q,b)->{
            var sub=q.subquery(Long.class);var h=sub.from(PostHashtag.class);sub.select(h.get("idPost"));sub.where(b.equal(h.get("tag"),tag.toLowerCase(Locale.ROOT)));
            var hijos=q.subquery(Long.class);var ch=hijos.from(ComentarioHashtag.class);var c=hijos.from(Comentario.class);hijos.select(c.get("idPost"));
            hijos.where(b.equal(ch.get("idComentario"),c.get("idComentario")),b.isTrue(c.get("activo")),b.equal(ch.get("tag"),tag.toLowerCase(Locale.ROOT)));
            return b.or(p.get("idPost").in(sub),p.get("idPost").in(hijos));};
    }
    private Specification<Post> guardadoPor(Long usuario) {
        return (p,q,b)->{var sub=q.subquery(Long.class);var g=sub.from(PostGuardado.class);sub.select(g.get("idPost"));sub.where(b.equal(g.get("idUsuario"),usuario));return p.get("idPost").in(sub);};
    }
    public Page<PostResumenResponse> mapear(Page<Post> page,SocialUsuarioActualDto u) { return new PageImpl<>(resumenes(page.getContent(),u),page.getPageable(),page.getTotalElements()); }
    public PostResumenResponse resumen(Long id,SocialUsuarioActualDto u) { return resumenes(List.of(acceso.visible(id)),u).getFirst(); }
    public List<PostResumenResponse> resumenes(List<Post> lista,SocialUsuarioActualDto u) {
        if(lista.isEmpty()) return List.of();
        var identidades=autores.autores(lista.stream().map(Post::getAutorId).toList());
        List<Long> ids=lista.stream().map(Post::getIdPost).toList();
        List<Long> idsEquipo=lista.stream().map(Post::getIdEquipoAutor).filter(Objects::nonNull).distinct().toList();
        Map<Long,SocialEquipoPerfilDto> perfiles=new HashMap<>();
        for(int i=0;i<idsEquipo.size();i+=100) for(var e:equipos.obtenerPerfilesPorIds(idsEquipo.subList(i,Math.min(i+100,idsEquipo.size())))) perfiles.put(e.idEquipo(),e);
        Map<Long,Long> totalesComentario=new HashMap<>();for(var r:comentarios.contarBatch(ids)) totalesComentario.put((Long)r[0],(Long)r[1]);
        Map<Long,Map<String,Long>> contadores=new HashMap<>();for(var r:reacciones.contarBatch(ids)) contadores.computeIfAbsent((Long)r[0],x->new LinkedHashMap<>()).put(r[1].toString(),(Long)r[2]);
        Map<Long,String> propias=new HashMap<>();for(var r:reacciones.findByIdPostInAndIdUsuario(ids,u.idUsuario())) propias.put(r.getIdPost(),r.getTipo().name());
        Set<Long> favoritos=guardados.findByIdUsuarioAndIdPostIn(u.idUsuario(),ids).stream().map(PostGuardado::getIdPost).collect(Collectors.toSet());
        return lista.stream().map(p->{
            var porTipo=contadores.getOrDefault(p.getIdPost(),Map.of());var equipo=perfiles.get(p.getIdEquipoAutor());
            AutorResponse autorEquipo=equipo==null?null:new AutorResponse(null,null,equipo.nombre(),null,equipo.nombre(),equipo.idEquipo(),equipo.imagenUrl());
            return new PostResumenResponse(p.getIdPost(),p.getTipoContenido().name(),p.getRefId(),p.getIdEquipoAutor(),p.getTitulo(),p.getResumen(),p.getLugar(),p.getFechaEvento(),p.getHoraInicio(),p.getHoraFin(),p.getRefEstado(),p.getImagenUrl(),p.getCoordinadorNombre(),p.getEsOficial(),p.getFechaPublicacion(),autor(identidades.get(p.getAutorId())),autorEquipo,totalesComentario.getOrDefault(p.getIdPost(),0L),porTipo,porTipo.values().stream().mapToLong(Long::longValue).sum(),p.getSharesTotal(),propias.get(p.getIdPost()),favoritos.contains(p.getIdPost()),p.getOrigenPostId(),equipo,acceso.editable(p,u),acceso.eliminable(p,u));
        }).toList();
    }
    public PostDetalleResponse detalle(Long id,SocialUsuarioActualDto u) {
        Post p=acceso.visible(id);var r=resumen(id,u);
        return new PostDetalleResponse(id,r.tipoContenido(),r.refId(),r.idEquipoAutor(),r.titulo(),r.resumen(),r.lugar(),r.fechaEvento(),r.horaInicio(),r.horaFin(),r.estado(),r.imagenUrl(),imagenes.findByIdPostOrderByOrdenAsc(id).stream().map(PostImagen::getUrl).toList(),r.coordinadorNombre(),r.esOficial(),r.fechaPublicacion(),p.getFechaActualizacion(),r.autor(),r.autorEquipo(),u.esAdministrador()||p.getAutorId().equals(u.idUsuario())?p.getAlcanceTotal():null,r.totalComentarios(),r.totalShares(),comentarios(id,PageRequest.of(0,20,Sort.by("fechaComentario","idComentario")),u).getContent(),new ReaccionResumenResponse(r.totalReacciones(),r.reaccionesPorTipo(),r.reaccionDelUsuario()),p.getOrigenPostId()==null?null:resumen(p.getOrigenPostId(),u),menciones(TipoContenidoMencion.POST,id),postTags.findByIdPost(id).stream().map(PostHashtag::getTag).toList(),r.equipo(),r.puedoEditar(),r.puedoEliminar());
    }
    public AutorResponse autor(SocialUsuarioSnapshotDto a) { return new AutorResponse(a.idUsuario(),a.username(),a.nombreCompleto(),a.rolPrincipal(),a.nombreEquipo(),null,a.fotoUrl()); }
    public AutorResponse autor(Long id) { return autor(autores.autores(List.of(id)).get(id)); }
    public List<MencionResponse> menciones(TipoContenidoMencion tipo,Long id) {
        var lista=menciones.findByTipoContenidoAndIdContenido(tipo,id);
        var identidades=autores.autores(lista.stream().filter(m->m.getTipoMencion()==TipoMencion.USUARIO).map(m->m.getRefId()).toList());
        return lista.stream().map(m -> {
            String nombre=null,imagen=null,url=null;
            try {
                switch(m.getTipoMencion()) {
                    case USUARIO -> {var a=identidades.get(m.getRefId());nombre=a.nombreCompleto();imagen=a.fotoUrl();url="/api/v1/social/usuarios/"+m.getRefId()+"/actividad";}
                    case EQUIPO -> {var e=equipos.obtenerPorId(m.getRefId());nombre=e.nombre();imagen=e.logoUrl();url="/api/v1/social/muro/equipos/"+m.getRefId();}
                    case ACTIVIDAD -> {var a=actividades.obtenerActividadPorId(m.getRefId());if(a.isPresent()) {nombre=a.get().nombre();imagen=a.get().logoUrl();}url="/api/v1/social/muro/actividades/"+m.getRefId();}
                }
            }catch(upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException ausente) {nombre="[Contenido no disponible]";}
            return new MencionResponse(m.getTipoMencion(),m.getRefId(),nombre,imagen,url);
        }).toList();
    }
    public Page<ComentarioResponse> comentarios(Long post,Pageable page,SocialUsuarioActualDto u) {
        acceso.visible(post);Page<Comentario> raices=comentarios.raices(post,page);
        List<Long> ids=raices.stream().filter(c->Boolean.TRUE.equals(c.getActivo())).map(Comentario::getIdComentario).toList();
        List<Comentario> hijos=ids.isEmpty()?List.of():comentarios.findByIdComentarioPadreInAndActivoTrueOrderByFechaComentarioAscIdComentarioAsc(ids);
        List<Long> autoresIds=new ArrayList<>(raices.stream().map(Comentario::getIdUsuarioAutor).toList());hijos.forEach(c->autoresIds.add(c.getIdUsuarioAutor()));
        var mapa=autores.autores(autoresIds);
        List<ComentarioResponse> resultado=raices.stream().map(c -> comentarioDto(c,hijos.stream().filter(h->c.getIdComentario().equals(h.getIdComentarioPadre())).map(h->comentarioDto(h,List.of(),mapa,u)).toList(),mapa,u)).toList();
        return new PageImpl<>(resultado,page,raices.getTotalElements());
    }
    public ComentarioResponse comentario(Long id,SocialUsuarioActualDto u) {var c=acceso.comentarioVisible(id);return comentarioDto(c,List.of(),autores.autores(List.of(c.getIdUsuarioAutor())),u);}
    private ComentarioResponse comentarioDto(Comentario c,List<ComentarioResponse> respuestas,Map<Long,SocialUsuarioSnapshotDto> mapa,SocialUsuarioActualDto u) {
        boolean activo=Boolean.TRUE.equals(c.getActivo());
        return new ComentarioResponse(c.getIdComentario(),c.getIdComentarioPadre(),c.getIdUsuarioAutor(),autor(mapa.get(c.getIdUsuarioAutor())),activo?c.getContenido():"Comentario eliminado",activo&&Boolean.TRUE.equals(c.getEditado()),activo?c.getFechaEdicion():null,c.getFechaComentario(),activo?adjuntos.findByIdComentarioOrderByOrdenAsc(c.getIdComentario()).stream().map(ComentarioAdjunto::getUrl).toList():List.of(),activo?menciones(TipoContenidoMencion.COMENTARIO,c.getIdComentario()):List.of(),activo?comentarioTags.findByIdComentario(c.getIdComentario()).stream().map(ComentarioHashtag::getTag).toList():List.of(),activo?reaccionComentario(c.getIdComentario(),u):new ReaccionResumenResponse(0,Map.of(),null),respuestas,activo&&c.getIdUsuarioAutor().equals(u.idUsuario()),activo&&(u.esAdministrador()||c.getIdUsuarioAutor().equals(u.idUsuario())));
    }
    public ReaccionResumenResponse reaccionPost(Long id,SocialUsuarioActualDto u) {var r=resumen(id,u);return new ReaccionResumenResponse(r.totalReacciones(),r.reaccionesPorTipo(),r.reaccionDelUsuario());}
    public ReaccionResumenResponse reaccionComentario(Long id,SocialUsuarioActualDto u) {
        acceso.comentarioVisible(id);var lista=reaccionesComentario.findByIdComentario(id);Map<String,Long> tipos=new LinkedHashMap<>();
        lista.forEach(r->tipos.merge(r.getTipo().name(),1L,Long::sum));return new ReaccionResumenResponse(lista.size(),tipos,lista.stream().filter(r->r.getIdUsuario().equals(u.idUsuario())).map(r->r.getTipo().name()).findFirst().orElse(null));
    }
    public Page<ReaccionDetalleResponse> quienReacciono(Long id,boolean comentario,TipoReaccion tipo,Pageable page) {
        if(comentario) {
            acceso.comentarioVisible(id);Page<ReaccionComentario> r=tipo==null?reaccionesComentario.findByIdComentario(id,page):reaccionesComentario.findByIdComentarioAndTipo(id,tipo,page);
            var mapa=autores.autores(r.stream().map(ReaccionComentario::getIdUsuario).toList());return r.map(x->new ReaccionDetalleResponse(x.getIdUsuario(),autor(mapa.get(x.getIdUsuario())),x.getTipo(),x.getFechaReaccion()));
        }
        acceso.visible(id);Page<ReaccionPost> r=tipo==null?reacciones.findByIdPost(id,page):reacciones.findByIdPostAndTipo(id,tipo,page);
        var mapa=autores.autores(r.stream().map(ReaccionPost::getIdUsuario).toList());return r.map(x->new ReaccionDetalleResponse(x.getIdUsuario(),autor(mapa.get(x.getIdUsuario())),x.getTipo(),x.getFechaReaccion()));
    }
    private Specification<Comentario> comentariosVisibles() {
        return (c,q,b)->{var sub=q.subquery(Long.class);var p=sub.from(Post.class);sub.select(p.get("idPost"));sub.where(b.equal(p.get("idPost"),c.get("idPost")),visible(p,sub,b));return b.and(b.isTrue(c.get("activo")),b.exists(sub));};
    }
    public PerfilUsuarioResponse perfil(Long id,Pageable postsPage,Pageable commentsPage,SocialUsuarioActualDto u) {
        var pub=posts.findAll(visibles().and((p,q,b)->b.equal(p.get("autorId"),id)),postsPage);
        var com=comentarios.findAll(comentariosVisibles().and((c,q,b)->b.equal(c.get("idUsuarioAutor"),id)),commentsPage);
        var mapa=autores.autores(List.of(id));return new PerfilUsuarioResponse(autor(mapa.get(id)),mapear(pub,u),com.map(c->comentarioDto(c,List.of(),mapa,u)));
    }
    public MiContenidoResponse miContenido(String tab,Pageable page,SocialUsuarioActualDto u) {
        Page<PostResumenResponse> vacio=Page.empty();Page<ComentarioResponse> sinComentarios=Page.empty();
        switch(tab) {
            case "PUBLICACIONES" -> { return new MiContenidoResponse(perfil(u.idUsuario(),page,PageRequest.of(0,1),u).publicaciones(),sinComentarios,vacio,vacio); }
            case "COMENTARIOS" -> { return new MiContenidoResponse(vacio,perfil(u.idUsuario(),PageRequest.of(0,1),PageRequest.of(page.getPageNumber(),page.getPageSize(),Sort.by(Sort.Direction.DESC,"fechaComentario","idComentario")),u).comentarios(),vacio,vacio); }
            case "REACCIONES" -> {var ids=reacciones.findByIdUsuario(u.idUsuario()).stream().map(ReaccionPost::getIdPost).toList();return new MiContenidoResponse(vacio,sinComentarios,mapear(posts.findAll(visibles().and((p,q,b)->ids.isEmpty()?b.disjunction():p.get("idPost").in(ids)),page),u),vacio);}
            case "GUARDADOS" -> {return new MiContenidoResponse(vacio,sinComentarios,vacio,mapear(posts.findAll(visibles().and(guardadoPor(u.idUsuario())),page),u));}
            default -> throw new BusinessRuleException("Tab inválida");
        }
    }
    public BusquedaResponse buscar(String query,String tipo,Pageable page,SocialUsuarioActualDto u) {
        String q=query==null?"":query.trim().toLowerCase(Locale.ROOT);if(q.length()>200) throw new BusinessRuleException("Consulta demasiado larga");
        if(q.isBlank()) return new BusquedaResponse(tipo,Page.empty(),Page.empty(),List.of(),List.of());
        String patron="%"+q.replace("\\","\\\\").replace("%","\\%").replace("_","\\_")+"%";
        switch(tipo) {
            case "POST" -> {var filtro=visibles().and((p,c,b)->b.or(b.like(b.lower(p.get("titulo")),patron,'\\'),b.like(b.lower(p.get("resumen")),patron,'\\')));return new BusquedaResponse(tipo,mapear(posts.findAll(filtro,page),u),Page.empty(),List.of(),List.of());}
            case "COMENTARIO" -> {var com=comentarios.findAll(comentariosVisibles().and((c,cq,b)->b.like(b.lower(c.get("contenido")),patron,'\\')),PageRequest.of(page.getPageNumber(),page.getPageSize(),Sort.by(Sort.Direction.DESC,"fechaComentario","idComentario")));var mapa=autores.autores(com.stream().map(Comentario::getIdUsuarioAutor).toList());return new BusquedaResponse(tipo,Page.empty(),com.map(c->comentarioDto(c,List.of(),mapa,u)),List.of(),List.of());}
            case "AUTOR" -> {var encontrados=usuarios.buscar(q,20);var mapa=autores.autores(encontrados.stream().map(SocialUsuarioBusquedaDto::idUsuario).toList());return new BusquedaResponse(tipo,Page.empty(),Page.empty(),mapa.values().stream().map(this::autor).toList(),List.of());}
            case "HASHTAG" -> {Set<String> tags=new TreeSet<>(postTags.buscarTags("%"+q+"%"));tags.addAll(comentarioTags.buscarTags("%"+q+"%"));return new BusquedaResponse(tipo,Page.empty(),Page.empty(),List.of(),tags.stream().skip(page.getOffset()).limit(page.getPageSize()).toList());}
            default -> throw new BusinessRuleException("Tipo de búsqueda inválido");
        }
    }
    public List<TopHashtag> tendencias(int dias,int limite) {
        if(dias<1 || dias>365 || limite<1 || limite>50) throw new BusinessRuleException("Rango de tendencia inválido");
        return tendenciasRango(LocalDateTime.now(socialClock).minusDays(dias),LocalDateTime.now(socialClock),limite);
    }
    public List<TopHashtag> tendenciasRango(LocalDateTime desde,LocalDateTime hasta,int limite) {
        Map<String,Long> conteo=new HashMap<>();
        for(var h:postTags.contarRango(desde,hasta)) conteo.merge((String)h[0],(Long)h[1],Long::sum);
        for(var h:comentarioTags.contarRango(desde,hasta)) conteo.merge((String)h[0],(Long)h[1],Long::sum);
        return conteo.entrySet().stream().sorted(Map.Entry.<String,Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey())).limit(limite).map(e->new TopHashtag(e.getKey(),e.getValue())).toList();
    }
    public Object autocomplete(TipoMencion tipo,String query,int limite) {
        int cap=Math.clamp(limite,1,20);
        return switch(tipo) {case USUARIO->usuarios.buscar(query,cap);case ACTIVIDAD->actividades.buscarActividades(query,cap);case EQUIPO->equipos.buscar(query,cap);};
    }
}
