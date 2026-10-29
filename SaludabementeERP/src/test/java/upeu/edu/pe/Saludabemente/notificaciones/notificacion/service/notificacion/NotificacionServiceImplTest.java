package upeu.edu.pe.Saludabemente.notificaciones.notificacion.service.notificacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto.BandejaNotificacionDTO;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.entity.Notificacion;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.mapper.NotificacionMapper;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.mapper.NotificacionMapperImpl;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.repository.NotificacionRepository;
import upeu.edu.pe.Saludabemente.notificaciones.shared.enums.CanalNotificacion;
import upeu.edu.pe.Saludabemente.notificaciones.shared.enums.EstadoDelivery;
import upeu.edu.pe.Saludabemente.notificaciones.shared.enums.EstadoLectura;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceImplTest {

    private static final Long ID_PERSONA = 7L;

    @Mock
    private NotificacionRepository repository;

    private final NotificacionMapper mapper = new NotificacionMapperImpl();

    private NotificacionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificacionServiceImpl(repository, mapper);
    }

    @Test
    void listarBandeja_excluyeArchivadasYUsaElRepositorio() {
        when(repository.findByIdPersonaAndEstadoLecturaNotOrderByFechaEnvioDesc(
                ID_PERSONA, EstadoLectura.ARCHIVADA))
                .thenReturn(List.of(
                        notificacion("Sin leer", EstadoLectura.NO_LEIDA),
                        notificacion("Leída", EstadoLectura.LEIDA)));

        List<BandejaNotificacionDTO> bandeja = service.listarBandeja(ID_PERSONA);

        verify(repository).findByIdPersonaAndEstadoLecturaNotOrderByFechaEnvioDesc(
                ID_PERSONA, EstadoLectura.ARCHIVADA);
        assertThat(bandeja)
                .extracting(BandejaNotificacionDTO::getTitulo)
                .containsExactly("Sin leer", "Leída");
        assertThat(bandeja)
                .extracting(BandejaNotificacionDTO::getLeido)
                .containsExactly(false, true);
    }

    @Test
    void listarBandeja_mapeaElCanal() {
        when(repository.findByIdPersonaAndEstadoLecturaNotOrderByFechaEnvioDesc(
                ID_PERSONA, EstadoLectura.ARCHIVADA))
                .thenReturn(List.of(notificacion("Con canal", EstadoLectura.NO_LEIDA)));

        BandejaNotificacionDTO dto = service.listarBandeja(ID_PERSONA).getFirst();

        assertThat(dto.getCanal()).isEqualTo(CanalNotificacion.EMAIL);
    }

    @Test
    void contarNoLeidas_delegaEnElRepositorio() {
        when(repository.countByIdPersonaAndEstadoLectura(ID_PERSONA, EstadoLectura.NO_LEIDA))
                .thenReturn(2L);

        assertThat(service.contarNoLeidas(ID_PERSONA)).isEqualTo(2L);
    }

    private Notificacion notificacion(String titulo, EstadoLectura estado) {
        Notificacion n = new Notificacion();
        n.setIdPersona(ID_PERSONA);
        n.setCanal(CanalNotificacion.EMAIL);
        n.setTitulo(titulo);
        n.setMensaje("Mensaje de " + titulo);
        n.setOrigenModulo("metas");
        n.setEstadoLectura(estado);
        n.setEstadoDelivery(EstadoDelivery.ENVIADO);
        n.setFechaEnvio(LocalDateTime.now());
        return n;
    }
}
