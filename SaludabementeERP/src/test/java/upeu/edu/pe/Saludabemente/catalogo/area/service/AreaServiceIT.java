package upeu.edu.pe.Saludabemente.catalogo.area.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.request.AreaRequest;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResponse;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AreaServiceIT {

    @Autowired
    private AreaService areaService;

    // -------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------

    @Test
    void crearYBuscarArea() {
        AreaResponse creada = areaService.crear(areaRequest("Área test", "Descripción test"));

        assertThat(creada.id()).isNotNull();
        assertThat(creada.nombre()).isEqualTo("Área test");
        assertThat(creada.activo()).isTrue();

        var lista = areaService.buscar(null, Sort.by("nombre"));
        assertThat(lista).isNotEmpty();
    }

    @Test
    void crear_nombreDuplicado_lanzaBusinessRuleException() {
        areaService.crear(areaRequest("Área duplicada", null));

        assertThatThrownBy(() -> areaService.crear(areaRequest("Área duplicada", null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Área duplicada");
    }

    @Test
    void actualizar_cambiaNombreYDescripcion() {
        AreaResponse creada = areaService.crear(areaRequest("Área original", "desc original"));

        AreaRequest update = areaRequest("Área editada", "desc editada");
        AreaResponse actualizada = areaService.actualizar(creada.id(), update);

        assertThat(actualizada.nombre()).isEqualTo("Área editada");
        assertThat(actualizada.descripcion()).isEqualTo("desc editada");
    }

    @Test
    void inactivar_marcaActivoFalse() {
        AreaResponse creada = areaService.crear(areaRequest("Área a inactivar", null));

        areaService.inactivar(creada.id());

        AreaResponse consultada = areaService.obtener(creada.id());
        assertThat(consultada.activo()).isFalse();
    }

    @Test
    void inactivar_yaInactiva_lanzaBusinessRuleException() {
        AreaResponse creada = areaService.crear(areaRequest("Área doble baja", null));
        areaService.inactivar(creada.id());

        assertThatThrownBy(() -> areaService.inactivar(creada.id()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("inactiva");
    }

    // -------------------------------------------------------------------
    // Filtros
    // -------------------------------------------------------------------

    @Test
    void buscar_soloActivos_excluyeInactivos() {
        AreaResponse activa = areaService.crear(areaRequest("Área activa", null));
        AreaResponse inactiva = areaService.crear(areaRequest("Área inactiva", null));
        areaService.inactivar(inactiva.id());

        var activos = areaService.buscar(true, Sort.by("nombre"));
        var inactivos = areaService.buscar(false, Sort.by("nombre"));

        assertThat(activos).extracting(AreaResponse::nombre).contains(activa.nombre());
        assertThat(activos).extracting(AreaResponse::nombre).doesNotContain(inactiva.nombre());

        assertThat(inactivos).extracting(AreaResponse::nombre).contains(inactiva.nombre());
        assertThat(inactivos).extracting(AreaResponse::nombre).doesNotContain(activa.nombre());
    }

    // -------------------------------------------------------------------
    // Servicios publicos
    // -------------------------------------------------------------------

    @Test
    void listarResumen_soloDevuelveActivas() {
        AreaResponse activa = areaService.crear(areaRequest("Área resumen activa", null));
        AreaResponse inactiva = areaService.crear(areaRequest("Área resumen inactiva", null));
        areaService.inactivar(inactiva.id());

        var resumen = areaService.listarResumen();

        assertThat(resumen).extracting(r -> r.nombre()).contains(activa.nombre());
        assertThat(resumen).extracting(r -> r.nombre()).doesNotContain(inactiva.nombre());
    }

    @Test
    void buscarOFallar_idInexistente_lanzaResourceNotFoundException() {
        assertThatThrownBy(() -> areaService.buscarOFallar(999999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999999");
    }

    // -------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------

    private AreaRequest areaRequest(String nombre, String descripcion) {
        return new AreaRequest(nombre, descripcion);
    }
}