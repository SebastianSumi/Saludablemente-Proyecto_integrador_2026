package upeu.edu.pe.Saludabemente.catalogo.periodo.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.EstadoPeriodo;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.request.PeriodoRequest;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.response.PeriodoResponse;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PeriodoServiceIT {

    @Autowired
    private PeriodoService periodoService;

    @Test
    void crearYBuscarPeriodo() {
        PeriodoResponse creado = periodoService.crear(periodoRequest("2026-1", "Primer Semestre 2026"));

        assertThat(creado.getId()).isNotNull();
        assertThat(creado.getCodigo()).isEqualTo("2026-1");
        assertThat(creado.getEstado()).isEqualTo(EstadoPeriodo.PLANIFICADO);

        var lista = periodoService.buscar(null, Sort.by("codigo"));
        assertThat(lista).isNotEmpty();
    }

    @Test
    void crear_codigoDuplicado_lanzaBusinessRuleException() {
        periodoService.crear(periodoRequest("2026-1", "Primer"));

        assertThatThrownBy(() -> periodoService.crear(periodoRequest("2026-1", "Duplicado")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("2026-1");
    }

    @Test
    void crear_fechaInicioPosteriorAFin_lanzaBusinessRuleException() {
        PeriodoRequest request = new PeriodoRequest(
                "2026-1",
                "Fechas invertidas",
                LocalDate.of(2026, 12, 1),
                LocalDate.of(2026, 3, 1)
        );

        assertThatThrownBy(() -> periodoService.crear(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("anterior");
    }

    @Test
    void marcarVigente_exitoso() {
        PeriodoResponse creado = periodoService.crear(periodoRequest("2026-1", "Primer"));
        PeriodoResponse vigente = periodoService.marcarVigente(creado.getId());

        assertThat(vigente.getEstado()).isEqualTo(EstadoPeriodo.VIGENTE);

        PeriodoResponse vigenteConsultado = periodoService.obtenerVigente();
        assertThat(vigenteConsultado.getId()).isEqualTo(creado.getId());
    }

    @Test
    void marcarVigente_yaHayOtroVigente_lanzaBusinessRuleException() {
        PeriodoResponse p1 = periodoService.crear(periodoRequest("2026-1", "Primer"));
        periodoService.marcarVigente(p1.id());

        PeriodoResponse p2 = periodoService.crear(periodoRequest("2026-2", "Segundo"));

        assertThatThrownBy(() -> periodoService.marcarVigente(p2.id()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("VIGENTE");
    }

    @Test
    void obtenerVigente_sinVigente_lanzaResourceNotFoundException() {
        assertThatThrownBy(() -> periodoService.obtenerVigente())
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cerrar_exitoso_marcaCerradoEnYCerradoPor() {
        PeriodoResponse creado = periodoService.crear(periodoRequest("2026-1", "Primer"));
        periodoService.marcarVigente(creado.id());

        PeriodoResponse cerrado = periodoService.cerrar(creado.id());

        assertThat(cerrado.estado()).isEqualTo(EstadoPeriodo.CERRADO);
        assertThat(cerrado.cerradoEn()).isNotNull();
        assertThat(cerrado.cerradoPor()).isEqualTo("system");
    }

    @Test
    void cerrar_yaCerrado_lanzaBusinessRuleException() {
        PeriodoResponse creado = periodoService.crear(periodoRequest("2026-1", "Primer"));
        periodoService.cerrar(creado.id());

        assertThatThrownBy(() -> periodoService.cerrar(creado.id()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("CERRADO");
    }

    @Test
    void obtenerPorCodigo_existente_devuelvePeriodo() {
        periodoService.crear(periodoRequest("2026-1", "Primer"));

        PeriodoResponse encontrado = periodoService.obtenerPorCodigo("2026-1");
        assertThat(encontrado.codigo()).isEqualTo("2026-1");
    }

    @Test
    void obtenerPorCodigo_inexistente_lanzaResourceNotFoundException() {
        assertThatThrownBy(() -> periodoService.obtenerPorCodigo("2099-9"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("2099-9");
    }

    private PeriodoRequest periodoRequest(String codigo, String descripcion) {
        return new PeriodoRequest(
                codigo,
                descripcion,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 7, 31)
        );
    }
}