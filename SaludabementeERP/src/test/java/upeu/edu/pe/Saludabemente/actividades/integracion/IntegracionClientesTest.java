package upeu.edu.pe.Saludabemente.actividades.integracion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import upeu.edu.pe.Saludabemente.actividades.integracion.client.SeguridadClientService;
import upeu.edu.pe.Saludabemente.actividades.integracion.dto.CoordinadorSnapshotDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class IntegracionClientesTest {

    @Autowired
    private SeguridadClientService seguridadClient;

    @Test
    void seguridadClient_estaInyectado() {
        assertThat(seguridadClient).isNotNull();
    }

    @Test
    void listarCoordinadores_noLanzaExcepcion() {
        // Debe devolver al menos los coordinadores seed
        List<CoordinadorSnapshotDto> coordinadores = seguridadClient.listarCoordinadores();
        assertThat(coordinadores).isNotNull();
    }
}
