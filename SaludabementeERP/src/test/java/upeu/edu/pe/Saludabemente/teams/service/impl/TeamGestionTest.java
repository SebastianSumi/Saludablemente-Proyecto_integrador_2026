package upeu.edu.pe.Saludabemente.teams.service.impl;

import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import upeu.edu.pe.Saludabemente.teams.api.TeamAccesoApi;
import upeu.edu.pe.Saludabemente.teams.entity.Team;
import upeu.edu.pe.Saludabemente.teams.repository.TeamRepository;
import upeu.edu.pe.Saludabemente.teams.mapper.TeamMapper;
import upeu.edu.pe.Saludabemente.teams.dto.request.TeamRequest;
import upeu.edu.pe.Saludabemente.shared.storage.AlmacenamientoImagenService;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeamGestionTest {
    final TeamRepository repo = mock(TeamRepository.class);
    final TeamMapper mapper = mock(TeamMapper.class);
    final TeamAccesoApi acceso = mock(TeamAccesoApi.class);
    final AlmacenamientoImagenService imagenes = mock(AlmacenamientoImagenService.class);
    final TeamServiceImpl service = new TeamServiceImpl(repo, mapper, acceso, imagenes);
    final Team team = Team.builder().id(12L).nombre("Equipo").activo(true).idUsuarioLider(42L).imagenKey("anterior").build();
    @BeforeEach void preparar() {
        when(repo.findById(12L)).thenReturn(Optional.of(team));
        when(acceso.usuarioActual()).thenReturn(42L);
    }
    @Test void liderDeOtroEquipoNoPuedeEditar() {
        when(acceso.usuarioActual()).thenReturn(99L);
        assertThrows(AccessDeniedException.class, () -> service.actualizar(12L, new TeamRequest("Nuevo", "")));
        verify(repo, never()).save(any());
    }
    @Test void liderQueDejoDeSerMiembroNoPuedeEditar() {
        assertThrows(AccessDeniedException.class, () -> service.actualizar(12L, new TeamRequest("Nuevo", "")));
    }
    @Test void liderActivoEditaIdentidadSinCambiarLiderazgo() {
        when(acceso.esMiembroActivo(42L, 12L)).thenReturn(true);
        service.actualizar(12L, new TeamRequest("Nuevo", "Descripción", "#123456", "Slogan", "Juan 3:16", "Texto"));
        assertEquals("Nuevo", team.getNombre()); assertEquals("#123456", team.getColor());
        assertEquals(42L, team.getIdUsuarioLider()); verify(repo).save(team);
    }
    @Test void soloAdminAsignaLider() {
        assertThrows(AccessDeniedException.class, () -> service.asignarLider(12L, 42L));
    }
    @Test void liderNoPuedeCrearEquipos() {
        assertThrows(AccessDeniedException.class, () -> service.crear(new TeamRequest("Equipo", "")));
    }
    @Test void subirImagenDeOtroEquipoNoTocaMinio() {
        when(acceso.usuarioActual()).thenReturn(99L);
        assertThrows(AccessDeniedException.class, () -> service.subirImagen(12L,
                new MockMultipartFile("archivo", "logo.png", "image/png", png())));
        verifyNoInteractions(imagenes);
    }
    @Test void equipoInactivoNoSeEditaInclusoSiEsAdmin() {
        team.setActivo(false);
        when(acceso.esAdministrador()).thenReturn(true);
        assertThrows(BusinessRuleException.class, () -> service.actualizar(12L, new TeamRequest("Nuevo", "")));
    }
    @Test void imagenMayorAlLimiteNoSeAcepta() {
        assertThrows(BusinessRuleException.class, () -> TeamImagenValidator.validar(
                new MockMultipartFile("archivo", "logo.png", "image/png", new byte[5 * 1024 * 1024 + 1])));
    }
    @Test void adminNoPuedeAsignarPersonaAjena() {
        when(acceso.esAdministrador()).thenReturn(true);
        assertThrows(BusinessRuleException.class, () -> service.asignarLider(12L, 99L));
        verify(repo, never()).save(any());
    }
    @Test void adminAsignaMiembroActivo() {
        when(acceso.esAdministrador()).thenReturn(true);
        when(acceso.esMiembroActivo(99L, 12L)).thenReturn(true);
        service.asignarLider(12L, 99L); assertEquals(99L, team.getIdUsuarioLider());
    }
    @Test void mimeDeclaradoNoBasta() {
        assertThrows(BusinessRuleException.class, () -> TeamImagenValidator.validar(
                new MockMultipartFile("archivo", "logo.png", "image/png", "archivo de texto".getBytes())));
        verifyNoInteractions(imagenes);
    }
    @Test void svgNoPermitido() {
        assertThrows(BusinessRuleException.class, () -> TeamImagenValidator.validar(
                new MockMultipartFile("archivo", "logo.svg", "image/svg+xml", "<svg/>".getBytes())));
    }
    @Test void extensionNoPuedeContenerOtraRuta() {
        assertThrows(BusinessRuleException.class, () -> TeamImagenValidator.validar(
                new MockMultipartFile("archivo", "logo.png/archivo", "image/png", png())));
    }
    @Test void limpiaNuevoObjetoAlRollback() { comprobarLimpieza(TransactionSynchronization.STATUS_ROLLED_BACK, "nueva"); }
    @Test void eliminaAnteriorSoloTrasCommit() { comprobarLimpieza(TransactionSynchronization.STATUS_COMMITTED, "anterior"); }
    private void comprobarLimpieza(int status, String esperada) {
        when(acceso.esAdministrador()).thenReturn(true);
        when(imagenes.guardar(any(), eq("teams/12/logo"), eq(5L * 1024 * 1024))).thenReturn("nueva");
        TransactionSynchronizationManager.initSynchronization();
        try {
            service.subirImagen(12L, new MockMultipartFile("archivo", "logo.png", "image/png", png()));
            verify(imagenes, never()).eliminar(any());
            TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(status));
            verify(imagenes).eliminar(esperada);
        } finally { TransactionSynchronizationManager.clearSynchronization(); }
    }
    private byte[] png() { return new byte[]{(byte)137,80,78,71,13,10,26,10,0,0,0,0}; }
}
