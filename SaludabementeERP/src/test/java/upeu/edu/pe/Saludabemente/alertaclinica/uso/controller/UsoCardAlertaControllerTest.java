package upeu.edu.pe.Saludabemente.alertaclinica.uso.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import upeu.edu.pe.Saludabemente.alertaclinica.uso.dto.UsoCardRequest;
import upeu.edu.pe.Saludabemente.alertaclinica.uso.service.UsoCardAlertaService;
import upeu.edu.pe.Saludabemente.seguridad.usuario.repository.UsuarioRepository;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsoCardAlertaController.class)
class UsoCardAlertaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsoCardAlertaService service;

    /** Requerido por UsuarioRegistradoFilter, que @WebMvcTest también carga. */
    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void obtener_respondeOkConMapaDeUso() throws Exception {
        when(service.obtener()).thenReturn(Map.of("/alertaclinica/reglas/glucosa", 3));

        mockMvc.perform(get("/api/v1/alertaclinica/uso-cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['/alertaclinica/reglas/glucosa']").value(3));
    }

    @Test
    void registrar_rutaValida_respondeNoContent() throws Exception {
        UsoCardRequest request = new UsoCardRequest("/alertaclinica/reglas/glucosa");

        mockMvc.perform(post("/api/v1/alertaclinica/uso-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(service).registrar("/alertaclinica/reglas/glucosa");
    }

    @Test
    void registrar_rutaVacia_respondeBadRequest() throws Exception {
        UsoCardRequest request = new UsoCardRequest("  ");

        mockMvc.perform(post("/api/v1/alertaclinica/uso-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reiniciar_respondeNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/alertaclinica/uso-cards"))
                .andExpect(status().isNoContent());

        verify(service).reiniciar();
    }
}
