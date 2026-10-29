package upeu.edu.pe.socialcontracts;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.domain.*;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import jakarta.persistence.EntityManagerFactory;
import tools.jackson.databind.*;
import tools.jackson.databind.json.JsonMapper;
import upeu.edu.pe.Saludabemente.social.SocialModuleConfiguration;
import upeu.edu.pe.Saludabemente.social.job.PostVistaPurgaJob;
import upeu.edu.pe.Saludabemente.social.api.*;
import upeu.edu.pe.Saludabemente.social.api.dto.*;
import upeu.edu.pe.Saludabemente.social.post.dto.*;
import upeu.edu.pe.Saludabemente.social.post.entity.*;
import upeu.edu.pe.Saludabemente.social.post.repository.*;
import upeu.edu.pe.Saludabemente.social.post.service.*;
import upeu.edu.pe.Saludabemente.social.comentario.service.ComentarioService;
import upeu.edu.pe.Saludabemente.social.comentario.repository.ComentarioRepository;
import upeu.edu.pe.Saludabemente.social.reaccion.service.InteraccionService;
import upeu.edu.pe.Saludabemente.social.reaccion.repository.*;
import upeu.edu.pe.Saludabemente.social.moderacion.service.ModeracionService;
import upeu.edu.pe.Saludabemente.social.analitica.service.AnaliticaService;
import upeu.edu.pe.Saludabemente.social.post.controller.*;
import upeu.edu.pe.Saludabemente.social.comentario.controller.ComentarioController;
import upeu.edu.pe.Saludabemente.social.reaccion.controller.ReaccionController;
import upeu.edu.pe.Saludabemente.social.busqueda.controller.BusquedaController;
import upeu.edu.pe.Saludabemente.social.perfil.controller.PerfilController;
import upeu.edu.pe.Saludabemente.social.moderacion.controller.ModeracionController;
import upeu.edu.pe.Saludabemente.social.analitica.controller.AnaliticaController;
import upeu.edu.pe.Saludabemente.seguridad.api.social.*;
import upeu.edu.pe.Saludabemente.personal.api.social.*;
import upeu.edu.pe.Saludabemente.actividades.api.social.SocialActividadApi;
import upeu.edu.pe.Saludabemente.notificaciones.api.social.*;
import upeu.edu.pe.Saludabemente.shared.storage.*;
import upeu.edu.pe.Saludabemente.shared.storage.social.*;
import upeu.edu.pe.Saludabemente.exception.*;
import javax.sql.DataSource;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Contexto Spring/JPA explícito, solo Social y H2 en memoria. Nunca arranca Spring Boot. */
@SpringJUnitConfig(SocialBackendTest.Config.class)
class SocialBackendTest {
    @Configuration @EnableTransactionManagement
    @EnableJpaRepositories("upeu.edu.pe.Saludabemente.social")
    @ComponentScan(basePackages="upeu.edu.pe.Saludabemente.social",excludeFilters=@ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE,classes={SocialModuleConfiguration.class,PostVistaPurgaJob.class}))
    static class Config {
        @Bean(destroyMethod="close") DataSource dataSource() {
            // Mantiene viva la conexión DDL: H2 2.4 evalúa CHECK de enums con su sesión creadora.
            var d=new com.zaxxer.hikari.HikariDataSource();d.setJdbcUrl("jdbc:h2:mem:social_backend;MODE=Oracle;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS SLB_SOCIAL");
            d.setUsername("sa");d.setPassword("");d.setMaximumPoolSize(8);d.setMinimumIdle(1);d.setIdleTimeout(0);d.setMaxLifetime(0);return d;
        }
        @Bean LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource ds) {
            var f=new LocalContainerEntityManagerFactoryBean();f.setDataSource(ds);f.setPackagesToScan("upeu.edu.pe.Saludabemente.social");f.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            f.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto","create-drop","hibernate.show_sql","false"));return f;
        }
        @Bean PlatformTransactionManager transactionManager(EntityManagerFactory f) {return new JpaTransactionManager(f);}
        @Bean Clock socialClock() {return Clock.fixed(Instant.parse("2026-10-23T15:00:00Z"),ZoneId.of("America/Lima"));}
        @Bean SocialUsuarioApi usuarios() {return mock(SocialUsuarioApi.class);}
        @Bean SocialIdentidadApi identidad() {return mock(SocialIdentidadApi.class);}
        @Bean SocialEquipoApi equipos() {return mock(SocialEquipoApi.class);}
        @Bean SocialActividadApi actividades() {return mock(SocialActividadApi.class);}
        @Bean SocialNotificacionApi notificaciones() {return mock(SocialNotificacionApi.class);}
        @Bean AlmacenamientoImagenService imagenes() {return mock(AlmacenamientoImagenService.class);}
        @Bean AdjuntoSocialStorageApi storage(AlmacenamientoImagenService a) {return new SocialComentarioStorageService(a);}
    }
    @Autowired PostService postService;
    @Autowired SocialApiService sincronizacion;
    @Autowired SocialConsultaService consulta;
    @Autowired ComentarioService comentarios;
    @Autowired InteraccionService interacciones;
    @Autowired FijadoService fijados;
    @Autowired ModeracionService moderacion;
    @Autowired AnaliticaService analitica;
    @Autowired PostRepository posts;
    @Autowired PostVistaRepository vistas;
    @Autowired ComentarioRepository comentarioRepo;
    @Autowired ReaccionPostRepository reaccionRepo;
    @Autowired SocialUsuarioApi usuarios;
    @Autowired SocialIdentidadApi identidad;
    @Autowired SocialEquipoApi equipos;
    @Autowired SocialActividadApi actividades;
    @Autowired SocialNotificacionApi notificaciones;
    @Autowired AlmacenamientoImagenService imagenes;
    @Autowired DataSource ds;
    @Autowired PlatformTransactionManager tx;
    @Autowired org.springframework.context.ApplicationContext context;
    MockMvc mvc;
    JsonMapper json=JsonMapper.builder().build();
    final SocialUsuarioActualDto A=new SocialUsuarioActualDto(101L,"ana.lazaro","a@example.invalid",List.of("Colaborador"),false);
    final SocialUsuarioActualDto B=new SocialUsuarioActualDto(102L,"b","b@example.invalid",List.of("Colaborador"),false);
    final SocialUsuarioActualDto C=new SocialUsuarioActualDto(103L,"c","c@example.invalid",List.of("Colaborador"),false);
    final SocialUsuarioActualDto D=new SocialUsuarioActualDto(104L,"admin","d@example.invalid",List.of("Administrador"),true);
    final SocialUsuarioActualDto T=new SocialUsuarioActualDto(106L,"coordinador","t@example.invalid",List.of("Coordinador"),false);
    @BeforeEach void preparar() {
        var jdbc=new JdbcTemplate(ds);for(String tabla:List.of("POST_REPORTE","MENCION","REACCION_COMENTARIO","REACCION_POST","COMENTARIO_HASHTAG","COMENTARIO_ADJUNTO","COMENTARIO","POST_FIJADO","POST_GUARDADO","POST_VISTA","POST_HASHTAG","POST_IMAGEN","POST")) jdbc.update("delete from SLB_SOCIAL."+tabla);
        reset(usuarios,identidad,equipos,actividades,notificaciones,imagenes);
        when(usuarios.obtenerUsuarioActual()).thenReturn(A);
        when(usuarios.obtenerPorIds(anyCollection())).thenAnswer(inv -> ((Collection<Long>)inv.getArgument(0)).stream().filter(id->id!=105L).map(this::autor).toList());
        when(usuarios.obtenerPorUsername(anyString())).thenAnswer(inv -> "ana.lazaro".equals(inv.getArgument(0))?Optional.of(autor(101L)):Optional.empty());
        when(identidad.obtenerIdentidadActiva(anyLong())).thenAnswer(inv -> {Long id=inv.getArgument(0);return Optional.of(new SocialIdentidadDto(id,id+900,"x@example.invalid",List.of()));});
        when(equipos.esMiembroDeEquipo(anyLong(),anyLong())).thenAnswer(inv -> List.of(101L,102L,106L).contains(inv.getArgument(0))&&Long.valueOf(301).equals(inv.getArgument(1)));
        when(equipos.esAdminDeEquipo(anyLong(),anyLong())).thenAnswer(inv -> Long.valueOf(106).equals(inv.getArgument(0))&&Long.valueOf(301).equals(inv.getArgument(1)));
        when(equipos.miembrosActivosDeEquipo(301L)).thenReturn(List.of(101L,102L,106L));
        when(equipos.obtenerPorId(301L)).thenReturn(new SocialEquipoSnapshotDto(301L,"Bienestar",null,"https://example.invalid/equipo.png",true,3));
        when(equipos.obtenerPerfilesPorIds(anyList())).thenAnswer(inv -> ((List<Long>)inv.getArgument(0)).stream().filter(id->id==301L).map(id->new SocialEquipoPerfilDto(id,"Bienestar",null,"https://example.invalid/equipo.png","#27AE60",null,null,null,106L,new SocialLiderEquipoDto(106L,"lider","Líder",null,true),true,3)).toList());
        when(imagenes.construirUrlPublica(anyString())).thenAnswer(inv -> "https://example.invalid/bucket/"+inv.getArgument(0));
        mvc=MockMvcBuilders.standaloneSetup(context.getBean(PostController.class),context.getBean(MuroController.class),context.getBean(ComentarioController.class),context.getBean(ReaccionController.class),context.getBean(BusquedaController.class),context.getBean(PerfilController.class),context.getBean(ModeracionController.class),context.getBean(AnaliticaController.class)).setControllerAdvice(new GlobalExceptionHandler()).build();
    }
    SocialUsuarioSnapshotDto autor(Long id) {return new SocialUsuarioSnapshotDto(id,id==101L?"ana.lazaro":"u"+id,"x@example.invalid","Usuario "+id,null,List.of("Colaborador"),"Colaborador",null,true);}
    JsonNode fixture(String id) throws Exception {var root=json.readTree(Files.readString(Path.of("..","docs","social","datos-prueba.json")));for(var e:root.get("ejemplos"))if(id.equals(e.get("id").asText()))return e.get("body");throw new AssertionError(id);}
    <V> V fixture(String id,Class<V> type) throws Exception {return json.treeToValue(fixture(id),type);}
    Long crearPost() {return postService.crear(new CrearPostRequest("Título","Resumen",null,List.of()),A);}
    Long comentario(Long p) {return comentarios.crear(p,new CrearComentarioRequest("Comentario raíz",null,List.of()),B);}

    @Test void j01j02SincronizacionEsIdempotenteYRespetaModeracion() throws Exception {
        var a=fixture("J01",ActividadSocialDto.class);sincronizacion.sincronizarPostDeActividad(a);Long id=posts.findByTipoContenidoAndRefId(TipoContenidoPost.ACTIVIDAD,401L).orElseThrow().getIdPost();
        sincronizacion.sincronizarPostDeActividad(a);assertEquals(1,posts.count());assertEquals(id,posts.findByTipoContenidoAndRefId(TipoContenidoPost.ACTIVIDAD,401L).orElseThrow().getIdPost());
        moderacion.ocultarPost(id,false,D);sincronizacion.sincronizarPostDeActividad(a);assertFalse(posts.findById(id).orElseThrow().getActivo());
        var n=fixture("J02",NoticiaSocialDto.class);sincronizacion.sincronizarPostDeNoticia(n);sincronizacion.sincronizarPostDeNoticia(n);
        assertEquals(2,posts.count());assertEquals(1,consulta.muro("RECIENTES",ScopeFijado.GLOBAL,null,null,consulta.pagina(0,20),A).pinned().size());
    }
    @Test void rollbackProductorNoDejaPost() throws Exception {
        var a=fixture("J01",ActividadSocialDto.class);new TransactionTemplate(tx).executeWithoutResult(status->{sincronizacion.sincronizarPostDeActividad(a);status.setRollbackOnly();});assertEquals(0,posts.count());verifyNoInteractions(notificaciones);
    }
    @Test void j03j04j05j06j07CrudYPermisos() throws Exception {
        Long p=postService.crear(fixture("J03",CrearPostRequest.class),A);Long equipo=postService.crear(fixture("J04",CrearPostRequest.class),B);
        assertThrows(org.springframework.security.access.AccessDeniedException.class,()->postService.crear(fixture("J04",CrearPostRequest.class),C));
        Long soloImagen=postService.crear(fixture("J05",CrearPostRequest.class),A);assertNull(posts.findById(soloImagen).orElseThrow().getTitulo());
        postService.editar(p,fixture("J06",EditarPostRequest.class),A);assertEquals("Pausa saludable actualizada",posts.findById(p).orElseThrow().getTitulo());
        postService.editar(equipo,new EditarPostRequest("Equipo editado","Texto",List.of()),T);
        assertThrows(org.springframework.security.access.AccessDeniedException.class,()->postService.eliminar(equipo,T));
        assertThrows(BusinessRuleException.class,()->postService.crear(fixture("J07",CrearPostRequest.class),A));postService.eliminar(p,A);assertFalse(posts.findById(p).orElseThrow().getActivo());
        var dto=consulta.detalle(equipo,A);assertEquals("https://example.invalid/equipo.png",dto.equipo().imagenUrl());assertEquals(106L,dto.equipo().lider().idUsuario());
    }
    @Test void j08j09j10ComentariosUnNivelTombstoneYValidaciones() throws Exception {
        Long p=crearPost();Long raiz=comentarios.crear(p,fixture("J08",CrearComentarioRequest.class),B);
        Long hijo=comentarios.crear(p,new CrearComentarioRequest("Respuesta",raiz,List.of()),C);
        assertThrows(BusinessRuleException.class,()->comentarios.crear(p,new CrearComentarioRequest("Nieto",hijo,List.of()),A));
        Long otro=crearPost();assertThrows(BusinessRuleException.class,()->comentarios.crear(otro,new CrearComentarioRequest("Padre ajeno",raiz,List.of()),A));
        comentarios.editar(p,raiz,fixture("J10",EditarComentarioRequest.class),B);assertTrue(comentarioRepo.findById(raiz).orElseThrow().getEditado());
        comentarios.eliminar(p,raiz,B);assertFalse(comentarioRepo.findById(hijo).orElseThrow().getActivo());
        var pagina=consulta.comentarios(p,PageRequest.of(0,20,Sort.by("fechaComentario")),A);assertEquals("Comentario eliminado",pagina.getContent().getFirst().contenido());assertTrue(pagina.getContent().getFirst().respuestas().isEmpty());
    }
    @Test void j11AdjuntosRealesDelPrefijoYLimites() {
        Long p=crearPost();String url="https://example.invalid/bucket/social/comentarios/"+p+"/abc.png";
        Long id=comentarios.crear(p,new CrearComentarioRequest("Foto",null,List.of(url)),B);assertEquals(List.of(url),consulta.comentario(id,A).adjuntosUrls());
        assertThrows(BusinessRuleException.class,()->comentarios.crear(p,new CrearComentarioRequest("Ajeno",null,List.of("https://otro.invalid/foto.png")),B));
        assertThrows(BusinessRuleException.class,()->comentarios.crear(p,new CrearComentarioRequest("Quinto",null,List.of(url,url,url,url,url)),B));
    }
    @Test void j12j13ReaccionesReemplazanYSeBorranFisicamente() throws Exception {
        Long p=crearPost();interacciones.reaccionar(p,fixture("J12",ReaccionarRequest.class).tipo(),B);interacciones.reaccionar(p,fixture("J13",ReaccionarRequest.class).tipo(),B);
        assertEquals(1,reaccionRepo.count());assertEquals("LOVE",consulta.reaccionPost(p,B).reaccionDelUsuario());
        assertEquals(1,consulta.quienReacciono(p,false,TipoReaccion.LOVE,PageRequest.of(0,20)).getTotalElements());
        Long c=comentario(p);interacciones.reaccionarComentario(c,TipoReaccion.APLAUSO,A);assertEquals(1,consulta.reaccionComentario(c,A).total());
        interacciones.reaccionar(p,null,B);assertEquals(0,reaccionRepo.count());
    }
    @Test void j14j15PinsCupoScopeYCuarto409() throws Exception {
        for(int i=0;i<3;i++)fijados.fijar(crearPost(),fixture("J14",FijarRequest.class),D);
        Long cuarto=crearPost();assertThrows(BusinessRuleException.class,()->fijados.fijar(cuarto,fixture("J14",FijarRequest.class),D));
        fijados.fijar(cuarto,fixture("J15",FijarRequest.class),D);assertEquals(1,fijados.activos(ScopeFijado.EQUIPO,301L).size());
        assertThrows(org.springframework.security.access.AccessDeniedException.class,()->fijados.fijar(cuarto,new FijarRequest(ScopeFijado.ACTIVIDAD,401L,null),A));
    }
    @Test void j16ShareRootRestauracionContadoresYOriginalOculto() throws Exception {
        Long p=crearPost();var s=interacciones.compartir(p,fixture("J16",CompartirRequest.class),B);var otro=interacciones.compartir(s.idPostShare(),new CompartirRequest(null,null),C);
        assertEquals(p,posts.findById(otro.idPostShare()).orElseThrow().getOrigenPostId());assertEquals(2L,posts.findById(p).orElseThrow().getSharesTotal());
        assertThrows(BusinessRuleException.class,()->interacciones.compartir(p,new CompartirRequest(null,null),B));
        postService.eliminar(s.idPostShare(),B);assertEquals(1L,posts.findById(p).orElseThrow().getSharesTotal());assertEquals(s.idPostShare(),interacciones.compartir(p,new CompartirRequest(null,null),B).idPostShare());
        moderacion.ocultarPost(p,false,D);assertThrows(ResourceNotFoundException.class,()->consulta.detalle(s.idPostShare(),A));
    }
    @Test void alcanceSoloDetalleUnicoYGuardadosPrivados() {
        Long p=crearPost();interacciones.registrarVista(p,A);interacciones.registrarVista(p,D);assertEquals(0L,posts.findById(p).orElseThrow().getAlcanceTotal());
        consulta.muro("RECIENTES",ScopeFijado.GLOBAL,null,null,consulta.pagina(0,20),B);assertEquals(0L,posts.findById(p).orElseThrow().getAlcanceTotal());
        interacciones.registrarVista(p,B);interacciones.registrarVista(p,B);assertEquals(1L,consulta.detalle(p,A).alcanceTotal());assertNull(consulta.detalle(p,B).alcanceTotal());
        interacciones.guardar(p,true,B);assertEquals(1,consulta.miContenido("GUARDADOS",consulta.pagina(0,20),B).guardados().getTotalElements());assertEquals(0,consulta.miContenido("GUARDADOS",consulta.pagina(0,20),A).guardados().getTotalElements());
    }
    @Test void j17HashtagsMencionesBusquedaYReparseo() throws Exception {
        Long p=crearPost();Long c=comentarios.crear(p,fixture("J17",CrearComentarioRequest.class),B);var r=consulta.comentario(c,A);assertEquals(List.of("salud"),r.hashtags());assertEquals(101L,r.menciones().getFirst().refId());
        assertEquals(1,consulta.buscar("actividad","COMENTARIO",consulta.pagina(0,20),A).comentarios().getTotalElements());
        comentarios.editar(p,c,new EditarComentarioRequest("Ahora #Bienestar @no.existe"),B);assertEquals(List.of("bienestar"),consulta.comentario(c,A).hashtags());assertTrue(consulta.comentario(c,A).menciones().isEmpty());
    }
    @Test void j18j19ReportesUnicosResueltosYOcultamiento() throws Exception {
        Long p=crearPost();var rep=moderacion.reportar(p,null,fixture("J18",ReportarRequest.class),C);
        assertThrows(BusinessRuleException.class,()->moderacion.reportar(p,null,fixture("J18",ReportarRequest.class),C));
        var res=moderacion.resolver(rep.idReporte(),fixture("J19",ResolverReporteRequest.class),D);assertEquals("OCULTADO",res.accionTomada());assertEquals("Se verificó el motivo del reporte.",res.comentarioRevision());assertFalse(posts.findById(p).orElseThrow().getActivo());
        assertThrows(BusinessRuleException.class,()->moderacion.resolver(rep.idReporte(),new ResolverReporteRequest(EstadoReporte.RECHAZADO,null),D));
        moderacion.ocultarPost(p,true,D);assertTrue(posts.findById(p).orElseThrow().getActivo());
    }
    @Test void eventosSoloTrasCommitYSinAutoAvisos() {
        Long p=crearPost();reset(notificaciones);comentario(p);verify(notificaciones).enviar(argThat(a->a.idPersonaDestinatario().equals(1001L)&&a.tipo()==TipoAvisoSocial.SOCIAL_COMENTARIO_POST));
        reset(notificaciones);new TransactionTemplate(tx).executeWithoutResult(status->{comentarios.crear(p,new CrearComentarioRequest("Rollback",null,List.of()),B);status.setRollbackOnly();});verifyNoInteractions(notificaciones);
        comentarios.crear(p,new CrearComentarioRequest("Propio",null,List.of()),A);verifyNoInteractions(notificaciones);
    }
    @Test void metricasYPerfilPublicoConComentarios() {
        Long p=crearPost();comentario(p);interacciones.reaccionar(p,TipoReaccion.LIKE,B);
        var m=analitica.metricas(LocalDate.of(2026,10,1),LocalDate.of(2026,10,31),D);assertEquals(1,m.totalPosts());assertEquals(1,m.totalComentarios());assertEquals(1,m.totalReacciones());assertEquals(2,m.usuariosActivos());
        assertThrows(org.springframework.security.access.AccessDeniedException.class,()->analitica.metricas(LocalDate.of(2026,10,1),LocalDate.of(2026,10,31),A));
        var perfil=consulta.perfil(B.idUsuario(),consulta.pagina(0,20),PageRequest.of(0,20,Sort.by("fechaComentario")),A);assertEquals(1,perfil.comentarios().getTotalElements());
    }
    @Test void jsonHttpRealValidacionYPrivacidad() throws Exception {
        var respuesta=mvc.perform(post("/api/v1/social/posts").contentType(MediaType.APPLICATION_JSON).content(fixture("J03").toString())).andExpect(status().isCreated()).andReturn();
        Long id=json.readTree(respuesta.getResponse().getContentAsString()).get("idPost").asLong();
        mvc.perform(post("/api/v1/social/posts").contentType(MediaType.APPLICATION_JSON).content(fixture("J07").toString())).andExpect(status().isConflict());
        when(usuarios.obtenerUsuarioActual()).thenReturn(B);mvc.perform(get("/api/v1/social/posts/"+id)).andExpect(status().isOk()).andExpect(jsonPath("$.alcanceTotal").doesNotExist());
        mvc.perform(post("/api/v1/social/posts/"+id+"/reacciones").contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":\"OTRO\"}")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/social/admin/metricas?desde=2026-10-01&hasta=2026-10-31")).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/social/muro?size=51")).andExpect(status().isConflict());
    }
    @Test void inactivoRecibe409DesdeAdaptadorSinAceptarAutorDelCliente() throws Exception {
        when(usuarios.obtenerUsuarioActual()).thenThrow(new UsuarioSocialInactivoException("Inactivo"));mvc.perform(post("/api/v1/social/posts").contentType(MediaType.APPLICATION_JSON).content(fixture("J03").toString())).andExpect(status().isConflict());assertEquals(0,posts.count());
    }
    @Test void treceTablasEnSchemaCorrectoYConstraintsReales() {
        var jdbc=new JdbcTemplate(ds);assertEquals(13,jdbc.queryForObject("select count(*) from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA='SLB_SOCIAL'",Integer.class));
        Post p=new Post();p.setTipoContenido(TipoContenidoPost.ACTIVIDAD);p.setRefId(401L);p.setAutorId(101L);p.setFechaPublicacion(LocalDateTime.now(context.getBean(Clock.class)));posts.saveAndFlush(p);
        Post duplicado=new Post();duplicado.setTipoContenido(TipoContenidoPost.ACTIVIDAD);duplicado.setRefId(401L);duplicado.setAutorId(102L);duplicado.setFechaPublicacion(p.getFechaPublicacion());
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,()->posts.saveAndFlush(duplicado));
        var repo=context.getBean(upeu.edu.pe.Saludabemente.social.moderacion.repository.PostReporteRepository.class);
        var r=new upeu.edu.pe.Saludabemente.social.moderacion.entity.PostReporte();r.setIdPost(p.getIdPost());r.setIdComentario(1L);r.setIdUsuarioReporta(103L);r.setMotivo(MotivoReporte.SPAM);r.setFechaReporte(p.getFechaPublicacion());
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,()->repo.saveAndFlush(r));
    }
    @Test void concurrenciaDeVistasCuentaUnaVez() throws Exception {
        Long p=crearPost();try(var executor=java.util.concurrent.Executors.newFixedThreadPool(4)) {
            List<java.util.concurrent.Callable<Void>> tareas=new ArrayList<>();for(int i=0;i<8;i++)tareas.add(()->{interacciones.registrarVista(p,B);return null;});
            for(var f:executor.invokeAll(tareas))f.get(15,java.util.concurrent.TimeUnit.SECONDS);
        }
        assertEquals(1L,posts.findById(p).orElseThrow().getAlcanceTotal());assertEquals(1,vistas.count());
    }
    @Test void concurrenciaDePinsRespetaCupo() throws Exception {
        List<Long> ids=new ArrayList<>();for(int i=0;i<4;i++)ids.add(crearPost());int correctos=0;
        try(var executor=java.util.concurrent.Executors.newFixedThreadPool(4)) {
            List<java.util.concurrent.Callable<Boolean>> tareas=new ArrayList<>();for(Long id:ids)tareas.add(()->{try{fijados.fijar(id,new FijarRequest(ScopeFijado.GLOBAL,null,null),D);return true;}catch(BusinessRuleException cupo){return false;}});
            for(var f:executor.invokeAll(tareas))if(f.get(15,java.util.concurrent.TimeUnit.SECONDS))correctos++;
        }
        assertEquals(3,correctos);assertEquals(3,fijados.activos(ScopeFijado.GLOBAL,null).size());
    }
    @Test void purga180DiasConservaTotalYPermiteNuevaVistaSegunEnmienda() {
        Long p=crearPost();interacciones.registrarVista(p,B);Clock clock=context.getBean(Clock.class);
        new TransactionTemplate(tx).executeWithoutResult(s->{vistas.findByIdPostAndIdUsuario(p,B.idUsuario()).orElseThrow().setFechaUltimaVista(LocalDateTime.now(clock).minusDays(181));});
        new TransactionTemplate(tx).executeWithoutResult(s->new PostVistaPurgaJob(vistas,clock).purgar());
        assertEquals(0,vistas.count());assertEquals(1L,posts.findById(p).orElseThrow().getAlcanceTotal());interacciones.registrarVista(p,B);assertEquals(2L,posts.findById(p).orElseThrow().getAlcanceTotal());
    }
    @Test void shareModeradoNoSeRestauraPorAutorYCuentaActivos() {
        Long p=crearPost();var share=interacciones.compartir(p,new CompartirRequest(null,null),B);moderacion.ocultarPost(share.idPostShare(),false,D);
        assertEquals(0L,posts.findById(p).orElseThrow().getSharesTotal());assertThrows(BusinessRuleException.class,()->interacciones.compartir(p,new CompartirRequest(null,null),B));
        moderacion.ocultarPost(share.idPostShare(),true,D);assertEquals(1L,posts.findById(p).orElseThrow().getSharesTotal());
    }
    @Test void restauracionNoReviveComentariosEliminadosPorAutor() {
        Long p=crearPost();Long c=comentario(p);comentarios.eliminar(p,c,B);assertThrows(BusinessRuleException.class,()->moderacion.ocultarComentario(c,true,D));
    }
    @Test void hashtagsDeComentarioNaveganAlPostYTendenciaRespetaRango() {
        Long p=crearPost();comentarios.crear(p,new CrearComentarioRequest("Texto #Salud",null,List.of()),B);
        assertEquals(1,consulta.muro("RECIENTES",ScopeFijado.GLOBAL,null,"salud",consulta.pagina(0,20),A).posts().getTotalElements());
        assertEquals("salud",consulta.buscar("sal","HASHTAG",consulta.pagina(0,20),A).hashtags().getFirst());
        assertEquals(1,consulta.tendencias(7,10).getFirst().total());
        assertTrue(analitica.metricas(LocalDate.of(2026,9,1),LocalDate.of(2026,9,30),D).hashtagsTendencia().isEmpty());
        moderacion.ocultarPost(p,false,D);assertTrue(consulta.tendencias(7,10).isEmpty());
    }
    @Test void losCincoTiposDeAvisoResuelvenPersonaYExcluyenAutor() {
        Long p=crearPost();Long c=comentario(p);comentarios.crear(p,new CrearComentarioRequest("Respuesta",c,List.of()),C);
        comentarios.crear(p,new CrearComentarioRequest("@ana.lazaro mencionada",null,List.of()),B);interacciones.reaccionar(p,TipoReaccion.LOVE,B);
        postService.crear(new CrearPostRequest("Equipo","Publicación",301L,List.of()),B);
        var captor=org.mockito.ArgumentCaptor.forClass(SocialAvisoDto.class);verify(notificaciones,atLeast(5)).enviar(captor.capture());
        var tipos=captor.getAllValues().stream().map(SocialAvisoDto::tipo).collect(java.util.stream.Collectors.toSet());assertEquals(Set.of(TipoAvisoSocial.values()),tipos);
        assertTrue(captor.getAllValues().stream().allMatch(a->a.idPersonaDestinatario()>=1001L));
    }
}
