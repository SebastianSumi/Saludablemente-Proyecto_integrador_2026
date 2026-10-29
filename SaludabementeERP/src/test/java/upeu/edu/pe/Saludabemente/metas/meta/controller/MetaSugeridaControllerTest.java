package upeu.edu.pe.Saludabemente.metas.meta.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.metas.meta.dto.MetaResponseDTO;
import upeu.edu.pe.Saludabemente.metas.meta.dto.MetaSugeridaRequestDTO;
import upeu.edu.pe.Saludabemente.metas.meta.service.AuditoriaMetasService;
import upeu.edu.pe.Saludabemente.metas.meta.service.MetaService;
import upeu.edu.pe.Saludabemente.metas.shared.enums.*;
import upeu.edu.pe.Saludabemente.seguridad.api.metas.MetasUsuarioApi;
import upeu.edu.pe.Saludabemente.seguridad.api.metas.UsuarioActualDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MetaSugeridaController.class)
class MetaSugeridaControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean MetaService metaService;
    @MockitoBean AuditoriaMetasService auditoriaMetas;
    @MockitoBean MetasUsuarioApi seguridadApi;

    private static final UsuarioActualDTO USUARIO = new UsuarioActualDTO(
            3L, 1L, "barbara.bernedo@upeu.edu.pe", "Barbara", List.of("Colaborador"));

    @Test
    void crear_conPayloadValido_respondeCreated() throws Exception {
        when(seguridadApi.tieneAlgunRol("Colaborador", "Administrador")).thenReturn(true);
        when(seguridadApi.obtenerUsuarioActual()).thenReturn(USUARIO);

        MetaSugeridaRequestDTO dto = new MetaSugeridaRequestDTO();
        dto.setIdPersona(1L);
        dto.setTipoMeta("porcentajeGrasaVisceral");
        dto.setDescripcion("Reducir grasa visceral al nivel 9");
        dto.setValorInicial(new BigDecimal("12.0"));
        dto.setValorObjetivo(new BigDecimal("9.0"));
        dto.setFechaLimite(LocalDate.now().plusMonths(6));
        dto.setPilar(PilarBienestar.NUTRICION);

        MetaResponseDTO response = new MetaResponseDTO(
                30L, 1L, null, null, null,
                PilarBienestar.NUTRICION, ModalidadMeta.CUANTITATIVA,
                TipoOrigen.PERSONAL, TipoCalculo.ULTIMO_VALOR,
                "porcentajeGrasaVisceral", null, "Reducir grasa visceral al nivel 9",
                new BigDecimal("12.0"), new BigDecimal("9.0"), new BigDecimal("12.0"),
                LocalDate.now(), dto.getFechaLimite(), null,
                EstadoMeta.ACTIVA);

        when(metaService.crearDesdeSugerencia(any(), eq(USUARIO.email())))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/metas/sugeridas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(30))
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.tipoOrigen").value("PERSONAL"));
    }

    @Test
    void crear_sinRolAutorizado_respondeConflict() throws Exception {
        when(seguridadApi.tieneAlgunRol("Colaborador", "Administrador")).thenReturn(false);

        MetaSugeridaRequestDTO dto = new MetaSugeridaRequestDTO();
        dto.setIdPersona(1L);
        dto.setTipoMeta("peso");
        dto.setDescripcion("Reducir peso");
        dto.setValorInicial(new BigDecimal("85"));
        dto.setValorObjetivo(new BigDecimal("80"));
        dto.setFechaLimite(LocalDate.now().plusMonths(6));
        dto.setPilar(PilarBienestar.NUTRICION);

        mockMvc.perform(post("/api/v1/metas/sugeridas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void crear_conFechaLimiteEnElPasado_respondeBadRequestOConflict() throws Exception {
        when(seguridadApi.tieneAlgunRol("Colaborador", "Administrador")).thenReturn(true);
        when(seguridadApi.obtenerUsuarioActual()).thenReturn(USUARIO);
        when(metaService.crearDesdeSugerencia(any(), any()))
                .thenThrow(new BusinessRuleException(
                        "La fecha limite debe ser posterior a hoy"));

        MetaSugeridaRequestDTO dto = new MetaSugeridaRequestDTO();
        dto.setIdPersona(1L);
        dto.setTipoMeta("peso");
        dto.setDescripcion("Reducir peso");
        dto.setValorInicial(new BigDecimal("85"));
        dto.setValorObjetivo(new BigDecimal("80"));
        dto.setFechaLimite(LocalDate.now().minusDays(1));
        dto.setPilar(PilarBienestar.NUTRICION);

        mockMvc.perform(post("/api/v1/metas/sugeridas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void crear_conPilarInvalido_respondeBadRequest() throws Exception {
        when(seguridadApi.tieneAlgunRol("Colaborador", "Administrador")).thenReturn(true);

        String jsonInvalido = """
                {
                  "idPersona": 1,
                  "tipoMeta": "peso",
                  "descripcion": "Reducir peso",
                  "valorInicial": 85,
                  "valorObjetivo": 80,
                  "fechaLimite": "2027-04-05",
                  "pilar": "BIENESTAR"
                }
                """;

        mockMvc.perform(post("/api/v1/metas/sugeridas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());
    }
}