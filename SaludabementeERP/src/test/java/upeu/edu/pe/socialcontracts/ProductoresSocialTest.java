package upeu.edu.pe.socialcontracts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import upeu.edu.pe.Saludabemente.actividades.agenda.entity.Actividad;
import upeu.edu.pe.Saludabemente.actividades.agenda.repository.ActividadRepository;
import upeu.edu.pe.Saludabemente.actividades.agenda.dto.request.ActividadRequest;
import upeu.edu.pe.Saludabemente.actividades.agenda.service.impl.AgendaServiceImpl;
import upeu.edu.pe.Saludabemente.actividades.configuracion.service.ConfiguracionService;
import upeu.edu.pe.Saludabemente.actividades.inscripcion.service.InscripcionService;
import upeu.edu.pe.Saludabemente.actividades.integracion.client.*;
import upeu.edu.pe.Saludabemente.actividades.imagen.service.ImagenService;
import upeu.edu.pe.Saludabemente.actividades.imagen.service.impl.ImagenServiceImpl;
import upeu.edu.pe.Saludabemente.actividades.imagen.repository.ActividadImagenRepository;
import upeu.edu.pe.Saludabemente.actividades.imagen.entity.ActividadImagen;
import upeu.edu.pe.Saludabemente.actividades.operacion.service.impl.OperacionServiceImpl;
import upeu.edu.pe.Saludabemente.actividades.operacion.dto.request.*;
import upeu.edu.pe.Saludabemente.actividades.api.social.*;
import upeu.edu.pe.Saludabemente.asistencias.qr.service.*;
import upeu.edu.pe.Saludabemente.shared.storage.AlmacenamientoImagenService;
import upeu.edu.pe.Saludabemente.social.api.SocialApiService;
import upeu.edu.pe.Saludabemente.social.api.dto.*;
import upeu.edu.pe.Saludabemente.teams.api.TeamApiService;
import upeu.edu.pe.Saludabemente.noticias.api.impl.NoticiaSocialSyncService;
import upeu.edu.pe.Saludabemente.noticias.api.social.*;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import org.springframework.mock.web.MockMultipartFile;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductoresSocialTest {
    Actividad actividad() {var a=new Actividad();a.setIdActividad(401L);a.setNombre("Actividad");a.setEstado("PROGRAMADA");a.setFechaActividad(LocalDate.of(2026,11,10));a.setHoraInicio(LocalTime.of(9,0));a.setHoraFin(LocalTime.of(10,0));a.setIdUsuarioCreador(101L);a.setIdUsuarioCoordinador(106L);return a;}
    @Test void creacionLlamaAContratoTrasGuardarYAsignaEquipoExplicito() {
        var repo=mock(ActividadRepository.class);var sync=mock(ActividadSocialSyncService.class);
        when(repo.save(any())).thenAnswer(i->{Actividad a=i.getArgument(0);a.setIdActividad(401L);return a;});
        var servicio=new AgendaServiceImpl(repo,mock(SeguridadClientService.class),mock(ConfiguracionService.class),mock(InscripcionService.class),mock(ImagenService.class),sync);
        var r=new ActividadRequest("Actividad",null,LocalDate.of(2026,11,10),LocalTime.of(9,0),LocalTime.of(10,0),"Campus",101L,106L,null,null,null,null,null,null,null,null,301L);
        servicio.crearActividadIndividual(r);verify(sync).validarEquipo(301L);verify(sync).sincronizar(401L);
        verify(repo).save(argThat(a->Long.valueOf(301).equals(a.getIdEquipoActividad())));
    }
    @ParameterizedTest @ValueSource(strings={"iniciar","finalizar","cancelar","reprogramar","transferir"})
    void cincoOperacionesSincronizan(String operacion) {
        var repo=mock(ActividadRepository.class);var sync=mock(ActividadSocialSyncService.class);var a=actividad();
        if(operacion.equals("finalizar"))a.setEstado("EN_CURSO");
        when(repo.findById(401L)).thenReturn(Optional.of(a));when(repo.save(any())).thenAnswer(i->i.getArgument(0));
        var servicio=new OperacionServiceImpl(repo,mock(QrActividadService.class),mock(QrStreamService.class),mock(SeguridadClientService.class),sync);
        switch(operacion) {case "iniciar"->servicio.iniciar(401L);case "finalizar"->servicio.finalizar(401L);case "cancelar"->servicio.cancelar(401L,new CancelacionRequest("Motivo"));case "reprogramar"->servicio.reprogramar(401L,new ReprogramacionRequest(LocalDate.of(2026,11,11),LocalTime.of(10,0),LocalTime.of(11,0)));case "transferir"->servicio.transferirCoordinador(401L,new TransferenciaRequest(102L));}
        verify(sync).sincronizar(401L);
    }
    @ParameterizedTest @ValueSource(strings={"subirLogo","eliminarLogo","subirImagen","eliminarImagen"})
    void cuatroCambiosDeImagenSincronizan(String operacion) {
        var repo=mock(ActividadRepository.class);var imgs=mock(ActividadImagenRepository.class);var storage=mock(AlmacenamientoImagenService.class);var sync=mock(ActividadSocialSyncService.class);var a=actividad();a.setLogoKey("logo.png");
        when(repo.findById(401L)).thenReturn(Optional.of(a));when(imgs.save(any())).thenAnswer(i->{ActividadImagen img=i.getArgument(0);img.setIdImagen(1L);return img;});
        var img=new ActividadImagen(401L,"foto.png","https://example.invalid/foto.png",null,1);img.setIdImagen(1L);when(imgs.findById(1L)).thenReturn(Optional.of(img));
        when(storage.guardar(any(),anyString(),anyLong())).thenReturn("imagen.png");when(storage.construirUrlPublica(any())).thenReturn("https://example.invalid/imagen.png");
        var servicio=new ImagenServiceImpl(repo,imgs,storage,sync);var archivo=new MockMultipartFile("archivo","foto.png","image/png",new byte[]{1});
        switch(operacion) {case "subirLogo"->servicio.subirLogo(401L,archivo);case "eliminarLogo"->servicio.eliminarLogo(401L);case "subirImagen"->servicio.subirImagen(401L,archivo,null);case "eliminarImagen"->servicio.eliminarImagen(401L,1L);}
        verify(sync).sincronizar(401L);
    }
    @Test void adaptadorActividadMapeaContratoYPropagaFallo() {
        var fuente=mock(SocialActividadApi.class);var social=mock(SocialApiService.class);var equipos=mock(TeamApiService.class);
        when(fuente.obtenerPublicacion(401L)).thenReturn(Optional.of(new ActividadPublicacionDto(401L,"Actividad","Resumen","Campus",LocalDate.of(2026,11,10),LocalTime.of(9,0),LocalTime.of(10,0),"PROGRAMADA",null,List.of(),"Coordinador",101L,301L)));
        var adapter=new ActividadSocialSyncService(fuente,social,equipos);adapter.sincronizar(401L);
        verify(social).sincronizarPostDeActividad(argThat(d->d.idActividad().equals(401L)&&d.idEquipoActividad().equals(301L)));
        doThrow(new BusinessRuleException("Fallo")).when(social).sincronizarPostDeActividad(any());assertThrows(BusinessRuleException.class,()->adapter.sincronizar(401L));
    }
    @Test void noticiasPublicadasOficialesSincronizanLasOtrasOcultan() {
        var fuente=mock(SocialNoticiaApi.class);var social=mock(SocialApiService.class);var adapter=new NoticiaSocialSyncService(fuente,social);
        when(fuente.obtenerNoticiaPorId(501L)).thenReturn(Optional.of(new NoticiaSnapshotDto(501L,"Noticia","Resumen",null,LocalDateTime.of(2026,10,23,9,0),"PUBLICADA",true,null,101L,null)));
        adapter.sincronizar(501L);verify(social).sincronizarPostDeNoticia(argThat(d->d.oficial()&&d.idNoticia().equals(501L)));
        when(fuente.obtenerNoticiaPorId(501L)).thenReturn(Optional.empty());adapter.sincronizar(501L);verify(social).ocultarPostDeNoticia(501L);
    }
    @ParameterizedTest @ValueSource(strings={"publicar","despublicar","archivar","subirImagen","eliminarImagen"})
    void flujosRealesDeNoticiasInvocanSincronizacion(String operacion) {
        var repo=mock(upeu.edu.pe.Saludabemente.noticias.noticia.repository.NoticiaRepository.class);
        var n=new upeu.edu.pe.Saludabemente.noticias.noticia.entity.NoticiaEntity();n.setId(501L);n.setTitulo("Noticia");n.setContenido("Contenido");n.setIdUsuarioAutor(101L);
        n.setEstado(operacion.equals("publicar")?upeu.edu.pe.Saludabemente.noticias.shared.enums.EstadoNoticia.BORRADOR:upeu.edu.pe.Saludabemente.noticias.shared.enums.EstadoNoticia.PUBLICADO);
        when(repo.findById(501L)).thenReturn(Optional.of(n));when(repo.save(any())).thenAnswer(i->i.getArgument(0));when(repo.saveAndFlush(any())).thenAnswer(i->i.getArgument(0));
        var mapper=mock(upeu.edu.pe.Saludabemente.noticias.noticia.mapper.NoticiaMapper.class);when(mapper.toResponse(any())).thenReturn(new upeu.edu.pe.Saludabemente.noticias.noticia.dto.NoticiaResponseDTO());
        var identidad=mock(upeu.edu.pe.Saludabemente.seguridad.api.noticias.NoticiasUsuarioApi.class);when(identidad.obtenerPorId(101L)).thenReturn(new upeu.edu.pe.Saludabemente.seguridad.api.noticias.UsuarioNoticiasDTO(101L,"a@example.invalid",1001L,"Ana",List.of(),true));
        var storage=mock(AlmacenamientoImagenService.class);when(storage.guardar(any(),anyString())).thenReturn("foto.png");when(storage.construirUrlPublica(any())).thenReturn("https://example.invalid/foto.png");
        var sync=mock(NoticiaSocialSyncService.class);
        var servicio=new upeu.edu.pe.Saludabemente.noticias.noticia.service.NoticiaServiceImpl(repo,
            mock(upeu.edu.pe.Saludabemente.noticias.noticia.repository.NoticiaTeamRepository.class),
            mock(upeu.edu.pe.Saludabemente.noticias.categoria.repository.CategoriaRepository.class),
            mock(upeu.edu.pe.Saludabemente.noticias.categoria.mapper.CategoriaMapper.class),mapper,storage,identidad,
            mock(upeu.edu.pe.Saludabemente.personal.api.PersonalNoticiasApiService.class),mock(TeamApiService.class),
            mock(upeu.edu.pe.Saludabemente.catalogo.area.service.AreaService.class),sync);
        switch(operacion) {case "publicar"->servicio.publicar(501L);case "despublicar"->servicio.despublicar(501L);case "archivar"->servicio.archivar(501L);case "subirImagen"->servicio.subirImagen(501L,new MockMultipartFile("imagen","a.png","image/png",new byte[]{1}));case "eliminarImagen"->servicio.eliminarImagen(501L);}
        verify(sync).sincronizar(501L);
    }
}
