package upeu.edu.pe.socialcontracts;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;
import upeu.edu.pe.Saludabemente.actividades.api.impl.SocialActividadApiImpl;
import upeu.edu.pe.Saludabemente.actividades.agenda.entity.Actividad;
import upeu.edu.pe.Saludabemente.actividades.agenda.repository.ActividadRepository;
import upeu.edu.pe.Saludabemente.actividades.imagen.entity.ActividadImagen;
import upeu.edu.pe.Saludabemente.actividades.imagen.repository.ActividadImagenRepository;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialUsuarioApi;
import upeu.edu.pe.Saludabemente.noticias.api.impl.SocialNoticiaApiImpl;
import upeu.edu.pe.Saludabemente.noticias.noticia.entity.NoticiaEntity;
import upeu.edu.pe.Saludabemente.noticias.noticia.repository.NoticiaRepository;
import upeu.edu.pe.Saludabemente.noticias.noticia.dto.NoticiaRequestDTO;
import upeu.edu.pe.Saludabemente.noticias.noticia.mapper.NoticiaMapperImpl;
import upeu.edu.pe.Saludabemente.noticias.shared.enums.EstadoNoticia;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContenidoContratoTest {
    private final ActividadRepository actividades = mock(ActividadRepository.class);
    private final ActividadImagenRepository imagenes = mock(ActividadImagenRepository.class);
    private final SocialUsuarioApi usuarios = mock(SocialUsuarioApi.class);
    private final SocialActividadApiImpl api = new SocialActividadApiImpl(actividades, imagenes, usuarios);

    @Test void ausenciaEsOptionalSinCambiarContratoAnterior() {
        assertTrue(api.obtenerActividadPorId(null).isEmpty());
        assertTrue(api.obtenerActividadPorId(999L).isEmpty());
        assertTrue(api.obtenerPublicacion(999L).isEmpty());
        assertTrue(api.buscarActividades(" ", 10).isEmpty());
        assertTrue(api.actividadesPorEquipo(null).isEmpty());
    }

    @Test void busquedaCapeaLimiteYConservaCancelada() {
        Actividad a = new Actividad(); a.setIdActividad(401L); a.setNombre("Caminata"); a.setEstado("CANCELADA");
        when(actividades.buscarParaSocial(eq("Caminata"), any())).thenReturn(List.of(a));
        assertEquals("CANCELADA", api.buscarActividades(" Caminata ", 500).getFirst().estado());
        ArgumentCaptor<Pageable> pagina = ArgumentCaptor.forClass(Pageable.class);
        verify(actividades).buscarParaSocial(eq("Caminata"), pagina.capture());
        assertEquals(20, pagina.getValue().getPageSize());
    }

    @Test void publicacionIncluyeGaleriaSinSubirYNoInfiereEquipo() {
        Actividad a = new Actividad(); a.setIdActividad(401L); a.setIdUsuarioCreador(101L);
        a.setLogoUrl("https://example.invalid/logo.png");
        ActividadImagen imagen = new ActividadImagen(); imagen.setImagenUrl("https://example.invalid/foto.webp");
        when(actividades.findById(401L)).thenReturn(Optional.of(a));
        when(imagenes.findByIdActividadOrderByOrdenAsc(401L)).thenReturn(List.of(imagen));
        var dto = api.obtenerPublicacion(401L).orElseThrow();
        assertNull(dto.idEquipoActividad());
        assertEquals(101L, dto.idUsuarioCreador());
        assertEquals(List.of(imagen.getImagenUrl()), dto.imagenesUrls());
        verifyNoInteractions(usuarios);
    }

    @Test void filtroEquipoUsaAtribucionExplicita() {
        when(actividades.findByIdEquipoActividadOrderByFechaActividadDescIdActividadDesc(301L))
                .thenReturn(List.of());
        assertTrue(api.actividadesPorEquipo(301L).isEmpty());
        verify(actividades).findByIdEquipoActividadOrderByFechaActividadDescIdActividadDesc(301L);
    }

    @Test void noticiaNoPublicadaNoSeExponeYOficialidadNoSeDeduce() {
        NoticiaRepository repo = mock(NoticiaRepository.class);
        NoticiaEntity n = new NoticiaEntity(); n.setId(501L); n.setTitulo("Noticia");
        n.setSubtitulo("NO es el resumen"); n.setIdUsuarioAutor(101L);
        when(repo.findById(501L)).thenReturn(Optional.of(n));
        SocialNoticiaApiImpl noticias = new SocialNoticiaApiImpl(repo);
        assertTrue(noticias.obtenerNoticiaPorId(501L).isEmpty());
        n.setEstado(EstadoNoticia.PUBLICADO);
        var dto = noticias.obtenerNoticiaPorId(501L).orElseThrow();
        assertFalse(dto.oficial()); assertNull(dto.resumen()); assertNull(dto.idEquipoAutor());
        assertEquals("PUBLICADA", dto.estado());
        n.setOficial(true); n.setResumen("Resumen explícito"); n.setIdEquipoAutor(301L);
        var oficial = noticias.obtenerNoticiaPorId(501L).orElseThrow();
        assertTrue(oficial.oficial()); assertEquals("Resumen explícito", oficial.resumen());
        assertEquals(301L, oficial.idEquipoAutor());
        n.setEstado(EstadoNoticia.ARCHIVADO);
        assertTrue(noticias.obtenerNoticiaPorId(501L).isEmpty());
    }

    @Test void mapperConservaDefaultFalseYCamposNuevos() {
        NoticiaMapperImpl mapper = new NoticiaMapperImpl();
        NoticiaRequestDTO request = new NoticiaRequestDTO();
        assertFalse(mapper.toEntity(request).getOficial());
        request.setOficial(true); request.setResumen("Resumen"); request.setIdEquipoAutor(301L);
        request.setUrlCompleta("https://example.invalid/noticias/501");
        NoticiaEntity n = mapper.toEntity(request);
        assertTrue(n.getOficial()); assertEquals("Resumen", n.getResumen());
        assertEquals(301L, mapper.toResponse(n).getIdEquipoAutor());
        assertTrue(mapper.toResponse(n).getOficial());
        assertFalse(NoticiaEntity.builder().build().getOficial());
    }
}
