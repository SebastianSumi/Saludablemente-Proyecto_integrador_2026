package upeu.edu.pe.socialcontracts;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import upeu.edu.pe.Saludabemente.exception.*;
import upeu.edu.pe.Saludabemente.personal.api.social.*;
import upeu.edu.pe.Saludabemente.seguridad.api.impl.SocialEquipoApiImpl;
import upeu.edu.pe.Saludabemente.seguridad.api.social.*;
import upeu.edu.pe.Saludabemente.teams.api.*;
import upeu.edu.pe.Saludabemente.teams.api.impl.TeamApiServiceImpl;
import upeu.edu.pe.Saludabemente.teams.entity.Team;
import upeu.edu.pe.Saludabemente.teams.mapper.TeamImagenMapper;
import upeu.edu.pe.Saludabemente.teams.repository.TeamRepository;
import java.nio.file.*;
import java.util.*;
import java.util.stream.LongStream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EquipoPerfilContratoTest {
    private final SocialIdentidadApi identidades = mock(SocialIdentidadApi.class);
    private final PersonaEquipoSocialApi personas = mock(PersonaEquipoSocialApi.class);
    private final TeamApiService teams = mock(TeamApiService.class);
    private final SocialUsuarioApi usuarios = mock(SocialUsuarioApi.class);
    private final SocialEquipoApiImpl api = new SocialEquipoApiImpl(identidades, personas, teams, usuarios);
    private final String imagen = "https://example.invalid/bucket/teams/301/imagen.png";

    private TeamPerfilDTO perfil(Long equipo, Long lider) {
        return new TeamPerfilDTO(equipo, "Bienestar", "Equipo institucional de bienestar", true,
                imagen, "#27AE60", "Cuidarnos juntos", "3 Juan 1:2", "Texto de referencia del equipo", lider);
    }

    private SocialUsuarioSnapshotDto lider(Long id) {
        return new SocialUsuarioSnapshotDto(id, "lider.equipo", "privado@example.invalid",
                "Líder del equipo", null, List.of("Colaborador"), "Colaborador", "Bienestar", true);
    }

    @Test void perfilCompletoCoincideConJsonYNoExponeEmailNiRoles() throws Exception {
        when(teams.obtenerPerfil(301L)).thenReturn(perfil(301L, 106L));
        when(usuarios.obtenerPorIds(List.of(106L))).thenReturn(List.of(lider(106L)));
        when(personas.contarPersonasActivasDeEquipo(301L)).thenReturn(3L);
        JsonMapper mapper = JsonMapper.builder().build();
        var esperado = mapper.treeToValue(mapper.readTree(Files.readString(
                Path.of("..", "docs", "social", "perfil-equipo-social.json"))).get("perfil"), SocialEquipoPerfilDto.class);
        var actual = api.obtenerPerfil(301L);
        assertEquals(esperado, actual);
        var json = mapper.valueToTree(actual);
        assertFalse(json.get("lider").has("email"));
        assertFalse(json.get("lider").has("roles"));
        assertFalse(json.has("imagenKey"));
    }

    @Test void equipoSinLiderNoConsultaUsuarios() {
        when(teams.obtenerPerfil(301L)).thenReturn(perfil(301L, null));
        assertNull(api.obtenerPerfil(301L).lider());
        verifyNoInteractions(usuarios);
    }

    @Test void liderAusenteOInactivoMantieneIdYPlaceholder() {
        when(teams.obtenerPerfil(301L)).thenReturn(perfil(301L, 106L));
        when(usuarios.obtenerPorIds(List.of(106L))).thenReturn(List.of());
        var dto = api.obtenerPerfil(301L);
        assertEquals(106L, dto.idUsuarioLider());
        assertEquals("[Usuario eliminado]", dto.lider().nombreCompleto());
        assertFalse(dto.lider().activo());
    }

    @Test void batchLideresNoDependeDeOrdenYDeduplicaIdentidades() {
        when(teams.obtenerPerfilesPorIds(List.of(301L, 302L, 303L))).thenReturn(List.of(
                perfil(301L, 106L), perfil(302L, 109L), perfil(303L, 106L)));
        when(usuarios.obtenerPorIds(List.of(106L, 109L))).thenReturn(List.of(lider(109L), lider(106L)));
        var perfiles = api.obtenerPerfilesPorIds(List.of(301L, 302L, 303L));
        assertEquals(106L, perfiles.get(0).lider().idUsuario());
        assertEquals(109L, perfiles.get(1).lider().idUsuario());
        assertEquals(106L, perfiles.get(2).lider().idUsuario());
        verify(usuarios, times(1)).obtenerPorIds(List.of(106L, 109L));
        verify(teams, never()).obtenerPerfil(anyLong());
    }

    @Test void limite100PerfilesYEquipoAusente() {
        assertThrows(BusinessRuleException.class, () -> api.obtenerPerfilesPorIds(LongStream.rangeClosed(1, 101).boxed().toList()));
        assertTrue(api.obtenerPerfilesPorIds(Arrays.asList(null, null)).isEmpty());
        assertThrows(ResourceNotFoundException.class, () -> api.obtenerPerfil(999L));
        verifyNoInteractions(usuarios);
    }

    @Test void imagenLlegaASnapshotMembresiaYBusqueda() {
        var snapshot = new TeamSnapshotDTO(301L, "Bienestar", null, true);
        when(teams.obtenerPorId(301L)).thenReturn(snapshot);
        when(teams.obtenerPerfil(301L)).thenReturn(perfil(301L, 106L));
        assertEquals(imagen, api.obtenerPorId(301L).logoUrl());
        when(identidades.obtenerIdentidadActiva(102L)).thenReturn(Optional.of(
                new SocialIdentidadDto(102L, 1002L, "b@example.invalid", List.of("Colaborador"))));
        when(personas.equipoDePersonaActiva(1002L)).thenReturn(Optional.of(301L));
        assertEquals(imagen, api.equiposDeUsuario(102L).getFirst().logoEquipoUrl());
        when(teams.listarEquiposActivos()).thenReturn(List.of(snapshot));
        when(teams.obtenerPerfilesPorIds(List.of(301L))).thenReturn(List.of(perfil(301L, 106L)));
        assertEquals(imagen, api.buscar("bien", 5).getFirst().logoUrl());
        assertFalse(api.esAdminDeEquipo(102L, 301L));
    }

    @Test void snapshotsBatchSeAsocianPorIdNoPorOrden() {
        when(teams.obtenerEquiposPorIds(List.of(301L, 302L))).thenReturn(List.of(
                new TeamSnapshotDTO(301L, "Uno", null, true), new TeamSnapshotDTO(302L, "Dos", null, true)));
        when(teams.obtenerPerfilesPorIds(List.of(301L, 302L))).thenReturn(List.of(
                new TeamPerfilDTO(302L, "Dos", null, true, "https://example.invalid/dos.png", null, null, null, null, null),
                perfil(301L, null)));
        var resultado = api.obtenerPorIds(List.of(301L, 302L));
        assertEquals(imagen, resultado.get(0).logoUrl());
        assertEquals("https://example.invalid/dos.png", resultado.get(1).logoUrl());
    }

    @Test void liderVisualNoRecibeAdministracionImplicitamente() {
        when(identidades.obtenerIdentidadActiva(106L)).thenReturn(Optional.of(
                new SocialIdentidadDto(106L, 1006L, "l@example.invalid", List.of("Colaborador"))));
        when(personas.equipoDePersonaActiva(1006L)).thenReturn(Optional.of(301L));
        when(teams.obtenerPorId(301L)).thenReturn(new TeamSnapshotDTO(301L, "Bienestar", null, true));
        when(teams.obtenerPerfil(301L)).thenReturn(perfil(301L, 106L));
        assertTrue(api.esMiembroDeEquipo(106L, 301L));
        assertFalse(api.esAdminDeEquipo(106L, 301L));
    }

    @Test void proveedorTeamsRetornaUrlPublicaSinExponerClaveYConservaSnapshotPrevio() {
        TeamRepository repo = mock(TeamRepository.class);
        TeamImagenMapper mapper = mock(TeamImagenMapper.class);
        var proveedor = new TeamApiServiceImpl(repo, mapper);
        Team t = new Team(); t.setId(301L); t.setNombre("Bienestar"); t.setActivo(true);
        t.setImagenKey("teams/301/imagen.png"); t.setIdUsuarioLider(106L);
        when(repo.findAllById(List.of(301L))).thenReturn(List.of(t));
        when(repo.findById(301L)).thenReturn(Optional.of(t));
        when(mapper.url("teams/301/imagen.png")).thenReturn(imagen);
        assertEquals(imagen, proveedor.obtenerPerfilesPorIds(List.of(301L)).getFirst().imagenUrl());
        assertEquals(106L, proveedor.obtenerPerfil(301L).idUsuarioLider());
        assertEquals(new TeamSnapshotDTO(301L, "Bienestar", null, true), proveedor.obtenerPorId(301L));
        assertTrue(proveedor.obtenerPerfilesPorIds(List.of()).isEmpty());
        assertThrows(BusinessRuleException.class, () -> proveedor.obtenerPerfilesPorIds(LongStream.rangeClosed(1, 101).boxed().toList()));
    }
}
