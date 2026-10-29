package upeu.edu.pe.Saludabemente.personal.persona.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResumen;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.personal.credencial.service.CredencialService;
import upeu.edu.pe.Saludabemente.personal.persona.dto.request.CelularUpdateRequest;
import upeu.edu.pe.Saludabemente.personal.persona.dto.request.PersonaRequest;
import upeu.edu.pe.Saludabemente.personal.persona.dto.response.PersonaResponse;
import upeu.edu.pe.Saludabemente.personal.persona.dto.response.PersonaResumen;
import upeu.edu.pe.Saludabemente.personal.persona.dto.PersonalAgregado;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Sexo;
import upeu.edu.pe.Saludabemente.personal.persona.service.PersonaService;
import upeu.edu.pe.Saludabemente.personal.preferencia.dto.request.PreferenciaRequest;
import upeu.edu.pe.Saludabemente.personal.preferencia.dto.response.PreferenciaResponse;
import upeu.edu.pe.Saludabemente.personal.preferencia.service.PreferenciaService;
import upeu.edu.pe.Saludabemente.teams.dto.response.TeamResumen;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PersonaController.class)
class PersonaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PersonaService personaService;

    @MockitoBean
    private PreferenciaService preferenciaService;

    @MockitoBean
    private CredencialService credencialService;

    // -------------------------------------------------------------------
    // GET /api/v1/personal
    // -------------------------------------------------------------------

    @Test
    void buscar_sinFiltros_respondeOkConLista() throws Exception {
        when(personaService.buscar(any(), any(), any(), any(), any(), any())).thenReturn(List.of(
                personaResponseEjemplo(1L, "Ana", "Lazaro", Sexo.F, "Chirimoya")
        ));

        mockMvc.perform(get("/api/v1/personal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombres").value("Ana"))
                .andExpect(jsonPath("$[0].areaTrabajo.nombre").value("CRAI"))
                .andExpect(jsonPath("$[0].equipo.nombre").value("Chirimoya"));
    }

    @Test
    void buscar_conFiltros_respondeOk() throws Exception {
        when(personaService.buscar(eq(true), eq(Sexo.F), eq(1L), any(), any(), any()))
                .thenReturn(List.of(
                        personaResponseEjemplo(1L, "Ana", "Lazaro", Sexo.F, "Chirimoya")
                ));

        mockMvc.perform(get("/api/v1/personal")
                        .param("estado", "true")
                        .param("sexo", "F")
                        .param("areaId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void buscar_conEquipoId_respondeOk() throws Exception {
        when(personaService.buscar(any(), any(), any(), eq(1L), any(), any())).thenReturn(List.of(
                personaResponseEjemplo(1L, "Ana", "Lazaro", Sexo.F, "Chirimoya")
        ));

        mockMvc.perform(get("/api/v1/personal").param("equipoId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void buscar_conTexto_respondeOk() throws Exception {
        when(personaService.buscar(any(), any(), any(), any(), eq("ana"), any())).thenReturn(List.of(
                personaResponseEjemplo(1L, "Ana", "Lazaro", Sexo.F, "Chirimoya")
        ));

        mockMvc.perform(get("/api/v1/personal").param("texto", "ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void buscar_conCampoDeOrdenNoPermitido_respondeBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/personal").param("ordenarPor", "campoInventado"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("campoInventado")));
    }

    // -------------------------------------------------------------------
    // GET /api/v1/personal/resumen
    // -------------------------------------------------------------------

    @Test
    void resumen_respondeAgregado() throws Exception {
        when(personaService.reporte()).thenReturn(new PersonalAgregado(10L, 8L, 3L, 7L));

        mockMvc.perform(get("/api/v1/personal/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPersonal").value(10))
                .andExpect(jsonPath("$.totalActivos").value(8))
                .andExpect(jsonPath("$.totalHombres").value(3))
                .andExpect(jsonPath("$.totalMujeres").value(7));
    }

    // -------------------------------------------------------------------
    // GET /api/v1/personal/{id}
    // -------------------------------------------------------------------

    @Test
    void obtener_idExistente_respondeOk() throws Exception {
        when(personaService.obtener(1L))
                .thenReturn(personaResponseEjemplo(1L, "Ana", "Lazaro", Sexo.F, "Chirimoya"));

        mockMvc.perform(get("/api/v1/personal/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.documentoIdentidad").value("12345678"))
                .andExpect(jsonPath("$.areaTrabajo.nombre").value("CRAI"))
                .andExpect(jsonPath("$.equipo.nombre").value("Chirimoya"));
    }

    @Test
    void obtener_idInexistente_respondeNotFound() throws Exception {
        when(personaService.obtener(999L))
                .thenThrow(new ResourceNotFoundException("Persona no encontrada: 999"));

        mockMvc.perform(get("/api/v1/personal/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // -------------------------------------------------------------------
    // GET /api/v1/personal/equipo/{idEquipo}
    // -------------------------------------------------------------------

    @Test
    void listarPorEquipo_respondeListaResumida() throws Exception {
        when(personaService.listarResumenPorEquipo(1L)).thenReturn(List.of(
                new PersonaResumen(1L, "Ana", "Lazaro", "Matallana")
        ));

        mockMvc.perform(get("/api/v1/personal/equipo/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombreCompleto").value("Ana Lazaro Matallana"));
    }

    @Test
    void listarPorEquipo_equipoInexistente_respondeNotFound() throws Exception {
        when(personaService.listarResumenPorEquipo(999L))
                .thenThrow(new ResourceNotFoundException("Equipo no encontrado: 999"));

        mockMvc.perform(get("/api/v1/personal/equipo/999"))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------
    // POST /api/v1/personal
    // -------------------------------------------------------------------

    @Test
    void crear_datosValidos_respondeCreated() throws Exception {
        PersonaRequest request = personaRequestEjemplo("951357327", Sexo.F, null);

        when(personaService.crear(any()))
                .thenReturn(personaResponseEjemplo(1L, "Ana", "Lazaro", Sexo.F, null));

        mockMvc.perform(post("/api/v1/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void crear_nombreVacio_respondeBadRequest() throws Exception {
        PersonaRequest request = new PersonaRequest(
                "", "Lazaro", null, "12345678", "951357327",
                LocalDate.of(1990, 3, 16), Sexo.F, null, null, null, null, null
        );

        mockMvc.perform(post("/api/v1/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("nombres")));
    }

    @Test
    void crear_celularConFormatoInvalido_respondeBadRequest() throws Exception {
        PersonaRequest request = personaRequestEjemplo("abc", Sexo.F, null);

        mockMvc.perform(post("/api/v1/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("celular")));
    }

    @Test
    void crear_documentoIdentidadVacio_respondeBadRequest() throws Exception {
        PersonaRequest request = new PersonaRequest(
                "Ana", "Lazaro", null, "", "951357327",
                LocalDate.of(1990, 3, 16), Sexo.F, null, null, null, null, null
        );

        mockMvc.perform(post("/api/v1/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("documentoIdentidad")));
    }

    @Test
    void crear_fechaNacimientoFutura_respondeBadRequest() throws Exception {
        PersonaRequest request = new PersonaRequest(
                "Ana", "Lazaro", null, "12345678", "951357327",
                LocalDate.now().plusDays(1), Sexo.F, null, null, null, null, null
        );

        mockMvc.perform(post("/api/v1/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("fechaNacimiento")));
    }

    @Test
    void crear_sexoInvalido_respondeBadRequest() throws Exception {
        String jsonInvalido = """
                {
                  "nombres": "Ana",
                  "apellidoPaterno": "Lazaro",
                  "documentoIdentidad": "12345678",
                  "celular": "951357327",
                  "fechaNacimiento": "1990-03-16",
                  "sexo": "X"
                }
                """;

        mockMvc.perform(post("/api/v1/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_celularDuplicado_respondeConflict() throws Exception {
        PersonaRequest request = personaRequestEjemplo("951357327", Sexo.F, null);

        when(personaService.crear(any()))
                .thenThrow(new BusinessRuleException(
                        "Ya existe una persona activa con el celular: 951357327"));

        mockMvc.perform(post("/api/v1/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("celular")));
    }

    @Test
    void crear_areaIdInexistente_respondeNotFound() throws Exception {
        PersonaRequest request = new PersonaRequest(
                "Ana", "Lazaro", null, "12345678", "951357327",
                LocalDate.of(1990, 3, 16), Sexo.F, 999L, null, null, null, null
        );

        when(personaService.crear(any()))
                .thenThrow(new ResourceNotFoundException("Area de trabajo no encontrada: 999"));

        mockMvc.perform(post("/api/v1/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------
    // PATCH /api/v1/personal/{id}/celular
    // -------------------------------------------------------------------

    @Test
    void cambiarCelular_valido_respondeOk() throws Exception {
        CelularUpdateRequest request = new CelularUpdateRequest("+51951999888");

        when(personaService.cambiarCelular(eq(1L), eq("+51951999888")))
                .thenReturn(personaResponseEjemplo(1L, "Ana", "Lazaro", Sexo.F, null));

        mockMvc.perform(patch("/api/v1/personal/1/celular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void cambiarCelular_formatoInvalido_respondeBadRequest() throws Exception {
        CelularUpdateRequest request = new CelularUpdateRequest("abc");

        mockMvc.perform(patch("/api/v1/personal/1/celular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------
    // DELETE /api/v1/personal/{id}
    // -------------------------------------------------------------------

    @Test
    void inactivar_exitoso_respondeNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/personal/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void inactivar_yaInactiva_respondeConflict() throws Exception {
        org.mockito.Mockito.doThrow(new BusinessRuleException("La persona ya está inactiva"))
                .when(personaService).inactivar(1L);

        mockMvc.perform(delete("/api/v1/personal/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("La persona ya está inactiva"));
    }

    // -------------------------------------------------------------------
    // Preferencias
    // -------------------------------------------------------------------

    @Test
    void upsertPreferencias_datosValidos_respondeOk() throws Exception {
        PreferenciaRequest request = new PreferenciaRequest(
                "WHATSAPP",
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                true
        );

        when(preferenciaService.upsertPreferencias(eq(1L), any()))
                .thenReturn(new PreferenciaResponse(
                        1L,
                        1L,
                        "WHATSAPP",
                        LocalTime.of(9, 0),
                        LocalTime.of(18, 0),
                        true
                ));

        mockMvc.perform(put("/api/v1/personal/1/preferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canalPreferido").value("WHATSAPP"));
    }

    @Test
    void obtenerPreferencias_existe_respondeOk() throws Exception {
        when(preferenciaService.obtenerPorPersona(1L)).thenReturn(
                new PreferenciaResponse(
                        1L,
                        1L,
                        "WHATSAPP",
                        LocalTime.of(9, 0),
                        LocalTime.of(18, 0),
                        true
                ));

        mockMvc.perform(get("/api/v1/personal/1/preferencias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personaId").value(1))
                .andExpect(jsonPath("$.canalPreferido").value("WHATSAPP"));
    }

    @Test
    void obtenerPreferencias_noExiste_respondeNotFound() throws Exception {
        when(preferenciaService.obtenerPorPersona(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Preferencia no encontrada para la persona: 999"));

        mockMvc.perform(get("/api/v1/personal/999/preferencias"))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------

    private PersonaRequest personaRequestEjemplo(String celular, Sexo sexo, Long equipoId) {
        return new PersonaRequest(
                "Ana",
                "Lazaro",
                "Matallana",
                "12345678",
                celular,
                LocalDate.of(1990, 3, 16),
                sexo,
                1L,
                null,
                null,
                null,
                equipoId
        );
    }

    private PersonaResponse personaResponseEjemplo(Long id, String nombres,
                                                   String apellido, Sexo sexo, String nombreEquipo) {
        return new PersonaResponse(
                id,
                nombres,
                apellido,
                "Matallana",
                "951357327",
                LocalDate.of(1990, 3, 16),
                35,
                sexo,
                new AreaResumen(1L, "CRAI"),
                null,
                null,
                null,
                nombreEquipo == null ? null : new TeamResumen(1L, nombreEquipo),
                true,
                "12345678",
                null,
                null
        );
    }
}