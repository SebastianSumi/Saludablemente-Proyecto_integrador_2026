package upeu.edu.pe.socialcontracts;

import org.junit.jupiter.api.Test;
import upeu.edu.pe.Saludabemente.exception.*;
import upeu.edu.pe.Saludabemente.seguridad.api.impl.*;
import upeu.edu.pe.Saludabemente.seguridad.api.social.*;
import upeu.edu.pe.Saludabemente.seguridad.usuario.entity.Usuario;
import upeu.edu.pe.Saludabemente.seguridad.usuario.repository.UsuarioRepository;
import upeu.edu.pe.Saludabemente.personal.api.social.*;
import upeu.edu.pe.Saludabemente.teams.api.*;
import upeu.edu.pe.Saludabemente.social.integracion.client.SocialUsuarioClientService;
import java.util.*;
import java.util.stream.LongStream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IdentidadEquipoContratoTest {
    private final SocialIdentidadApi identidades = mock(SocialIdentidadApi.class);
    private final PersonaEquipoSocialApi personas = mock(PersonaEquipoSocialApi.class);
    private final TeamApiService teams = mock(TeamApiService.class);
    private final SocialUsuarioApi usuarios = mock(SocialUsuarioApi.class);
    private final SocialEquipoApiImpl api = new SocialEquipoApiImpl(identidades, personas, teams, usuarios);

    private void miembro(List<String> roles) {
        when(identidades.obtenerIdentidadActiva(102L)).thenReturn(Optional.of(
                new SocialIdentidadDto(102L, 1002L, "b@example.invalid", roles)));
        when(personas.equipoDePersonaActiva(1002L)).thenReturn(Optional.of(301L));
        when(teams.obtenerPorId(301L)).thenReturn(new TeamSnapshotDTO(301L, "Bienestar", null, true));
    }

    @Test void miembroUsaPersonaRealSinIgualarIds() {
        miembro(List.of("Colaborador"));
        assertTrue(api.esMiembroDeEquipo(102L, 301L));
        assertFalse(api.esAdminDeEquipo(102L, 301L));
        verify(personas, atLeastOnce()).equipoDePersonaActiva(1002L);
        verify(personas, never()).equipoDePersonaActiva(102L);
    }

    @Test void coordinadorMiembroEsAdmin() {
        miembro(List.of("Coordinador"));
        assertTrue(api.esAdminDeEquipo(102L, 301L));
        assertFalse(api.esAdminDeEquipo(102L, 302L));
    }

    @Test void administradorGlobalNoSeConvierteEnAdminDeEquipo() {
        miembro(List.of("Administrador"));
        assertTrue(api.esMiembroDeEquipo(102L, 301L));
        assertFalse(api.esAdminDeEquipo(102L, 301L));
    }

    @Test void cuentaInactivaOPersonaSinMembresiaNoObtienePermisos() {
        when(identidades.obtenerIdentidadActiva(105L)).thenReturn(Optional.empty());
        assertFalse(api.esMiembroDeEquipo(105L, 301L));
        assertFalse(api.esAdminDeEquipo(105L, 301L));
        miembro(List.of("Coordinador"));
        when(personas.equipoDePersonaActiva(1002L)).thenReturn(Optional.empty());
        assertFalse(api.esAdminDeEquipo(102L, 301L));
    }

    @Test void equipoInactivoNoAutorizaNiNotifica() {
        miembro(List.of("Coordinador"));
        when(teams.obtenerPorId(301L)).thenReturn(new TeamSnapshotDTO(301L, "Bienestar", null, false));
        assertFalse(api.esMiembroDeEquipo(102L, 301L));
        assertTrue(api.miembrosActivosDeEquipo(301L).isEmpty());
    }

    @Test void destinatariosSeResuelvenPorLotesDePersonasYRetornanUsuarios() {
        when(teams.obtenerPorId(301L)).thenReturn(new TeamSnapshotDTO(301L, "Bienestar", null, true));
        List<Long> ids = LongStream.rangeClosed(1001, 1101).boxed().toList();
        when(personas.personasActivasDeEquipo(301L)).thenReturn(ids);
        when(identidades.obtenerPorPersonas(ids.subList(0, 100))).thenReturn(List.of(
                new SocialIdentidadDto(101L, 1001L, "a@example.invalid", List.of())));
        when(identidades.obtenerPorPersonas(ids.subList(100, 101))).thenReturn(List.of(
                new SocialIdentidadDto(201L, 1101L, "z@example.invalid", List.of())));
        assertEquals(List.of(101L, 201L), api.miembrosActivosDeEquipo(301L));
        verify(identidades).obtenerPorPersonas(ids.subList(0, 100));
        verify(identidades).obtenerPorPersonas(ids.subList(100, 101));
    }

    @Test void equipoAusenteEs404YBatchExcesivoEs409() {
        assertThrows(ResourceNotFoundException.class, () -> api.obtenerPorId(999L));
        assertThrows(BusinessRuleException.class, () -> api.obtenerPorIds(LongStream.range(1, 102).boxed().toList()));
        assertTrue(api.obtenerPorIds(List.of()).isEmpty());
    }

    @Test void identidadResuelveVinculoRealYOmitidos() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        Usuario usuario = new Usuario();
        usuario.setId(101L); usuario.setIdPersonaSnapshot(1001L); usuario.setActivo(true);
        when(repo.findById(101L)).thenReturn(Optional.of(usuario));
        SocialIdentidadApiImpl identidad = new SocialIdentidadApiImpl(repo);
        assertEquals(1001L, identidad.obtenerIdentidadActiva(101L).orElseThrow().idPersona());
        usuario.setActivo(false);
        assertTrue(identidad.obtenerIdentidadActiva(101L).isEmpty());
        assertTrue(identidad.obtenerIdentidadActiva(null).isEmpty());
        assertThrows(BusinessRuleException.class, () -> identidad.obtenerPorPersonas(LongStream.range(1, 102).boxed().toList()));
    }

    @Test void autores101SeIndexanPorIdConFallbackYOrdenIndependiente() {
        SocialUsuarioApi usuarios = mock(SocialUsuarioApi.class);
        List<Long> ids = LongStream.rangeClosed(1, 101).boxed().toList();
        SocialUsuarioSnapshotDto segundo = new SocialUsuarioSnapshotDto(2L, "b", null, "B", null,
                List.of(), null, null, true);
        when(usuarios.obtenerPorIds(ids.subList(0, 100))).thenReturn(List.of(segundo));
        when(usuarios.obtenerPorIds(ids.subList(100, 101))).thenReturn(List.of());
        var mapa = new SocialUsuarioClientService(usuarios).autores(ids);
        assertEquals(101, mapa.size());
        assertEquals("B", mapa.get(2L).nombreCompleto());
        assertEquals("[Usuario eliminado]", mapa.get(1L).nombreCompleto());
        assertFalse(mapa.get(101L).activo());
        verify(usuarios).obtenerPorIds(ids.subList(100, 101));
    }

    @Test void soloInactividadSeTraduceA409NoSeOcultaJWTInvalido() {
        SocialUsuarioApi usuarios = mock(SocialUsuarioApi.class);
        SocialUsuarioClientService client = new SocialUsuarioClientService(usuarios);
        when(usuarios.obtenerUsuarioActual()).thenThrow(new UsuarioSocialInactivoException("Inactivo"));
        assertThrows(BusinessRuleException.class, client::obtenerUsuarioActual);
        doThrow(new ResourceNotFoundException("Sin JWT")).when(usuarios).obtenerUsuarioActual();
        assertThrows(ResourceNotFoundException.class, client::obtenerUsuarioActual);
    }
}
