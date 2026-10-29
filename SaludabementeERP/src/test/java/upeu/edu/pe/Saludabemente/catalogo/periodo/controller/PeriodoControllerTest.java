package upeu.edu.pe.Saludabemente.catalogo.periodo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.EstadoPeriodo;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.request.PeriodoRequest;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.response.PeriodoResponse;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.response.PeriodoResumen;
import upeu.edu.pe.Saludabemente.catalogo.periodo.service.PeriodoService;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PeriodoController.class)
class PeriodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PeriodoService periodoService;

    // -------------------------------------------------------------------
    // GET /api/v1/periodos
    // -------------------------------------------------------------------

    @Test
    void listar_sinFiltro_respondeOkConLista() throws Exception {
        when(periodoService.buscar(eq(null), any())).thenReturn(List.of(
                periodoResponseEjemplo(1L, "2026-1", "Primer Semestre 2026", EstadoPeriodo.VIGENTE),
                periodoResponseEjemplo(2L, "2026-2", "Segundo Semestre 2026", EstadoPeriodo.PLANIFICADO)
        ));

        mockMvc.perform(get("/api/v1/periodos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].codigo").value("2026-1"))
                .andExpect(jsonPath("$[0].estado").value("VIGENTE"));
    }

    @Test
    void listar_porEstadoVigente_respondeOk() throws Exception {
        when(periodoService.buscar(eq(EstadoPeriodo.VIGENTE), any())).thenReturn(List.of(
                periodoResponseEjemplo(1L, "2026-1", "Primer Semestre 2026", EstadoPeriodo.VIGENTE)
        ));

        mockMvc.perform(get("/api/v1/periodos").param("estado", "VIGENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].estado").value("VIGENTE"));
    }

    // -------------------------------------------------------------------
    // GET /api/v1/periodos/resumen
    // -------------------------------------------------------------------

    @Test
    void listarResumen_respondeOk() throws Exception {
        when(periodoService.listarResumen()).thenReturn(List.of(
                new PeriodoResumen(1L, "2026-1", "Primer Semestre 2026", EstadoPeriodo.VIGENTE)
        ));

        mockMvc.perform(get("/api/v1/periodos/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].codigo").value("2026-1"));
    }

    // -------------------------------------------------------------------
    // GET /api/v1/periodos/vigente
    // -------------------------------------------------------------------

    @Test
    void obtenerVigente_existe_respondeOk() throws Exception {
        when(periodoService.obtenerVigente())
                .thenReturn(periodoResponseEjemplo(1L, "2026-1", "Primer Semestre 2026", EstadoPeriodo.VIGENTE));

        mockMvc.perform(get("/api/v1/periodos/vigente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("2026-1"))
                .andExpect(jsonPath("$.estado").value("VIGENTE"));
    }

    @Test
    void obtenerVigente_noExiste_respondeNotFound() throws Exception {
        when(periodoService.obtenerVigente())
                .thenThrow(new ResourceNotFoundException("No hay ningun periodo en estado VIGENTE"));

        mockMvc.perform(get("/api/v1/periodos/vigente"))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------
    // GET /api/v1/periodos/codigo/{codigo}
    // -------------------------------------------------------------------

    @Test
    void obtenerPorCodigo_existe_respondeOk() throws Exception {
        when(periodoService.obtenerPorCodigo("2026-1"))
                .thenReturn(periodoResponseEjemplo(1L, "2026-1", "Primer Semestre 2026", EstadoPeriodo.VIGENTE));

        mockMvc.perform(get("/api/v1/periodos/codigo/2026-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("2026-1"));
    }

    @Test
    void obtenerPorCodigo_inexistente_respondeNotFound() throws Exception {
        when(periodoService.obtenerPorCodigo("2099-9"))
                .thenThrow(new ResourceNotFoundException("Periodo no encontrado con codigo: 2099-9"));

        mockMvc.perform(get("/api/v1/periodos/codigo/2099-9"))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------
    // GET /api/v1/periodos/{id}
    // -------------------------------------------------------------------

    @Test
    void obtener_idExistente_respondeOk() throws Exception {
        when(periodoService.obtener(1L))
                .thenReturn(periodoResponseEjemplo(1L, "2026-1", "Primer Semestre 2026", EstadoPeriodo.VIGENTE));

        mockMvc.perform(get("/api/v1/periodos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void obtener_idInexistente_respondeNotFound() throws Exception {
        when(periodoService.obtener(999L))
                .thenThrow(new ResourceNotFoundException("Periodo no encontrado: 999"));

        mockMvc.perform(get("/api/v1/periodos/999"))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------
    // POST /api/v1/periodos
    // -------------------------------------------------------------------

    @Test
    void crear_datosValidos_respondeCreated() throws Exception {
        PeriodoRequest request = periodoRequestEjemplo("2026-2", "Segundo Semestre 2026");

        when(periodoService.crear(any()))
                .thenReturn(periodoResponseEjemplo(2L, "2026-2", "Segundo Semestre 2026", EstadoPeriodo.PLANIFICADO));

        mockMvc.perform(post("/api/v1/periodos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.estado").value("PLANIFICADO"));
    }

    @Test
    void crear_codigoConFormatoInvalido_respondeBadRequest() throws Exception {
        PeriodoRequest request = periodoRequestEjemplo("26-I", "Formato viejo");

        mockMvc.perform(post("/api/v1/periodos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("codigo")));
    }

    @Test
    void crear_codigoDuplicado_respondeConflict() throws Exception {
        PeriodoRequest request = periodoRequestEjemplo("2026-1", "Duplicado");

        when(periodoService.crear(any()))
                .thenThrow(new BusinessRuleException("Ya existe un periodo con el codigo: 2026-1"));

        mockMvc.perform(post("/api/v1/periodos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("2026-1")));
    }

    // -------------------------------------------------------------------
    // PUT /api/v1/periodos/{id}
    // -------------------------------------------------------------------

    @Test
    void actualizar_datosValidos_respondeOk() throws Exception {
        PeriodoRequest request = periodoRequestEjemplo("2026-1", "Primer Semestre 2026 - editado");

        when(periodoService.actualizar(eq(1L), any()))
                .thenReturn(periodoResponseEjemplo(1L, "2026-1", "Primer Semestre 2026 - editado", EstadoPeriodo.VIGENTE));

        mockMvc.perform(put("/api/v1/periodos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Primer Semestre 2026 - editado"));
    }

    // -------------------------------------------------------------------
    // PATCH /api/v1/periodos/{id}/vigente
    // -------------------------------------------------------------------

    @Test
    void marcarVigente_exitoso_respondeOk() throws Exception {
        when(periodoService.marcarVigente(2L))
                .thenReturn(periodoResponseEjemplo(2L, "2026-2", "Segundo Semestre 2026", EstadoPeriodo.VIGENTE));

        mockMvc.perform(patch("/api/v1/periodos/2/vigente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("VIGENTE"));
    }

    @Test
    void marcarVigente_yaHayOtroVigente_respondeConflict() throws Exception {
        when(periodoService.marcarVigente(2L))
                .thenThrow(new BusinessRuleException(
                        "Ya existe un periodo VIGENTE: 2026-1. Cierrelo antes de marcar otro como VIGENTE."));

        mockMvc.perform(patch("/api/v1/periodos/2/vigente"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("VIGENTE")));
    }

    // -------------------------------------------------------------------
    // PATCH /api/v1/periodos/{id}/cerrar
    // -------------------------------------------------------------------

    @Test
    void cerrar_exitoso_respondeOk() throws Exception {
        when(periodoService.cerrar(1L))
                .thenReturn(periodoResponseEjemplo(1L, "2026-1", "Primer Semestre 2026", EstadoPeriodo.CERRADO));

        mockMvc.perform(patch("/api/v1/periodos/1/cerrar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CERRADO"));
    }

    // -------------------------------------------------------------------
    // DELETE /api/v1/periodos/{id}
    // -------------------------------------------------------------------

    @Test
    void eliminar_exitoso_respondeNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/periodos/1"))
                .andExpect(status().isNoContent());
    }

    // -------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------

    private PeriodoRequest periodoRequestEjemplo(String codigo, String descripcion) {
        return new PeriodoRequest(
                codigo,
                descripcion,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 7, 31)
        );
    }

    private PeriodoResponse periodoResponseEjemplo(Long id, String codigo,
                                                   String descripcion, EstadoPeriodo estado) {
        return new PeriodoResponse(
                id,
                codigo,
                descripcion,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 7, 31),
                estado,
                null,
                null
        );
    }
}