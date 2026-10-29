package upeu.edu.pe.Saludabemente.catalogo.area.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.request.AreaRequest;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResponse;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResumen;
import upeu.edu.pe.Saludabemente.catalogo.area.service.AreaService;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AreaController.class)
class AreaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AreaService areaService;

    // -------------------------------------------------------------------
    // GET /api/v1/areas
    // -------------------------------------------------------------------

    @Test
    void listar_sinFiltro_respondeOkConLista() throws Exception {
        when(areaService.buscar(eq(null), any())).thenReturn(List.of(
                areaResponseEjemplo(1L, "CRAI", true),
                areaResponseEjemplo(2L, "Limpieza", true)
        ));

        mockMvc.perform(get("/api/v1/areas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("CRAI"))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    void listar_soloActivos_respondeOk() throws Exception {
        when(areaService.buscar(eq(true), any())).thenReturn(List.of(
                areaResponseEjemplo(1L, "CRAI", true)
        ));

        mockMvc.perform(get("/api/v1/areas").param("activo", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void listar_soloInactivos_respondeOk() throws Exception {
        when(areaService.buscar(eq(false), any())).thenReturn(List.of(
                areaResponseEjemplo(3L, "Área antigua", false)
        ));

        mockMvc.perform(get("/api/v1/areas").param("activo", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].activo").value(false));
    }

    // -------------------------------------------------------------------
    // GET /api/v1/areas/resumen
    // -------------------------------------------------------------------

    @Test
    void listarResumen_respondeOk() throws Exception {
        when(areaService.listarResumen()).thenReturn(List.of(
                new AreaResumen(1L, "CRAI"),
                new AreaResumen(2L, "Limpieza")
        ));

        mockMvc.perform(get("/api/v1/areas/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("CRAI"));
    }

    // -------------------------------------------------------------------
    // GET /api/v1/areas/{id}
    // -------------------------------------------------------------------

    @Test
    void obtener_idExistente_respondeOk() throws Exception {
        when(areaService.obtener(1L)).thenReturn(areaResponseEjemplo(1L, "CRAI", true));

        mockMvc.perform(get("/api/v1/areas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("CRAI"));
    }

    @Test
    void obtener_idInexistente_respondeNotFound() throws Exception {
        when(areaService.obtener(999L))
                .thenThrow(new ResourceNotFoundException("Area de trabajo no encontrada: 999"));

        mockMvc.perform(get("/api/v1/areas/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // -------------------------------------------------------------------
    // POST /api/v1/areas
    // -------------------------------------------------------------------

    @Test
    void crear_datosValidos_respondeCreated() throws Exception {
        AreaRequest request = new AreaRequest("Producción", "Área de producción");

        when(areaService.crear(any()))
                .thenReturn(areaResponseEjemplo(7L, "Producción", true));

        mockMvc.perform(post("/api/v1/areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.nombre").value("Producción"));
    }

    @Test
    void crear_nombreVacio_respondeBadRequest() throws Exception {
        AreaRequest request = new AreaRequest("", null);  // viola @NotBlank

        mockMvc.perform(post("/api/v1/areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("nombre")));
    }

    @Test
    void crear_nombreDuplicado_respondeConflict() throws Exception {
        AreaRequest request = new AreaRequest("CRAI", null);

        when(areaService.crear(any()))
                .thenThrow(new BusinessRuleException("Ya existe un area activa con el nombre: CRAI"));

        mockMvc.perform(post("/api/v1/areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("CRAI")));
    }

    // -------------------------------------------------------------------
    // PUT /api/v1/areas/{id}
    // -------------------------------------------------------------------

    @Test
    void actualizar_datosValidos_respondeOk() throws Exception {
        AreaRequest request = new AreaRequest("CRAI editado", "Nueva descripción");

        when(areaService.actualizar(eq(1L), any()))
                .thenReturn(areaResponseEjemplo(1L, "CRAI editado", true));

        mockMvc.perform(put("/api/v1/areas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("CRAI editado"));
    }

    // -------------------------------------------------------------------
    // DELETE /api/v1/areas/{id}
    // -------------------------------------------------------------------

    @Test
    void inactivar_exitoso_respondeNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/areas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void inactivar_yaInactiva_respondeConflict() throws Exception {
        org.mockito.Mockito.doThrow(
                        new BusinessRuleException("El area ya esta inactiva: CRAI"))
                .when(areaService).inactivar(1L);

        mockMvc.perform(delete("/api/v1/areas/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("inactiva")));
    }

    // -------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------

    private AreaResponse areaResponseEjemplo(Long id, String nombre, Boolean activo) {
        return new AreaResponse(
                id,
                nombre,
                "Descripción de " + nombre,
                activo
        );
    }
}