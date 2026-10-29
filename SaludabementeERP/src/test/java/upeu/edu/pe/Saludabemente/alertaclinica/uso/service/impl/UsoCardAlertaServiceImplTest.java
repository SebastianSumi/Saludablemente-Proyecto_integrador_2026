package upeu.edu.pe.Saludabemente.alertaclinica.uso.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import upeu.edu.pe.Saludabemente.alertaclinica.uso.entity.UsoCardAlerta;
import upeu.edu.pe.Saludabemente.alertaclinica.uso.repository.UsoCardAlertaRepository;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.seguridad.usuario.entity.Usuario;
import upeu.edu.pe.Saludabemente.seguridad.usuario.repository.UsuarioRepository;
import upeu.edu.pe.Saludabemente.seguridad.util.CurrentUserProvider;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsoCardAlertaServiceImplTest {

    private static final Long ID_USUARIO = 5L;
    private static final String EMAIL = "admin@saludablemente.pe";
    private static final String RUTA = "/alertaclinica/reglas/glucosa";

    @Mock
    private UsoCardAlertaRepository repository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private UsoCardAlertaServiceImpl service;

    private void autenticar() {
        when(currentUserProvider.obtenerEmailDesdeRequest()).thenReturn(Optional.of(EMAIL));
        when(usuarioRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(Usuario.builder().id(ID_USUARIO).email(EMAIL).build()));
    }

    @Test
    void obtener_devuelveMapaRutaConteo() {
        autenticar();
        when(repository.findByIdUsuario(ID_USUARIO)).thenReturn(List.of(
                uso(RUTA, 3),
                uso("/alertaclinica/reglas/imc", 1)
        ));

        Map<String, Integer> resultado = service.obtener();

        assertThat(resultado)
                .containsEntry(RUTA, 3)
                .containsEntry("/alertaclinica/reglas/imc", 1);
    }

    @Test
    void registrar_incrementaUsoExistente() {
        autenticar();
        when(repository.findByIdUsuarioAndRuta(ID_USUARIO, RUTA))
                .thenReturn(Optional.of(uso(RUTA, 2)));

        service.registrar(RUTA);

        ArgumentCaptor<UsoCardAlerta> captor = ArgumentCaptor.forClass(UsoCardAlerta.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getConteo()).isEqualTo(3);
    }

    @Test
    void registrar_creaUsoNuevoCuandoNoExiste() {
        autenticar();
        when(repository.findByIdUsuarioAndRuta(ID_USUARIO, RUTA)).thenReturn(Optional.empty());

        service.registrar(RUTA);

        ArgumentCaptor<UsoCardAlerta> captor = ArgumentCaptor.forClass(UsoCardAlerta.class);
        verify(repository).save(captor.capture());
        UsoCardAlerta guardado = captor.getValue();
        assertThat(guardado.getRuta()).isEqualTo(RUTA);
        assertThat(guardado.getIdUsuario()).isEqualTo(ID_USUARIO);
        assertThat(guardado.getConteo()).isEqualTo(1);
    }

    @Test
    void reiniciar_borraUsoDelUsuario() {
        autenticar();

        service.reiniciar();

        verify(repository).deleteByIdUsuario(ID_USUARIO);
    }

    @Test
    void obtener_sinAutenticacion_lanzaResourceNotFound() {
        when(currentUserProvider.obtenerEmailDesdeRequest()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtener())
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private UsoCardAlerta uso(String ruta, int conteo) {
        UsoCardAlerta u = new UsoCardAlerta();
        u.setRuta(ruta);
        u.setConteo(conteo);
        return u;
    }
}
