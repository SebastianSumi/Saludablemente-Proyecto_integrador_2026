package upeu.edu.pe.Saludabemente.alertaclinica.colaborador;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.controller.AlertaClinicaController;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto.AlertaClinicaResponse;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.AlertaClinica;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.mapper.AlertaClinicaMapper;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.repository.AlertaClinicaRepository;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.service.AlertaClinicaService;
import upeu.edu.pe.Saludabemente.alertaclinica.colaborador.controller.MisAlertasController;
import upeu.edu.pe.Saludabemente.alertaclinica.colaborador.service.MisAlertasServiceImpl;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.controller.EtiquetaClinicaController;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.*;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.service.*;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.parametro.controller.ParametroEvaluacionController;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.parametro.repository.ParametroEvaluacionRepository;
import upeu.edu.pe.Saludabemente.exception.GlobalExceptionHandler;
import upeu.edu.pe.Saludabemente.seguridad.api.*;
import upeu.edu.pe.Saludabemente.seguridad.config.*;
import upeu.edu.pe.Saludabemente.seguridad.usuario.repository.UsuarioRepository;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Filtro real de seguridad y conversion JWT real; servicios de datos aislados con mocks. */
@SpringJUnitConfig(MisAlertasSecurityTest.Config.class)
@WebAppConfiguration
class MisAlertasSecurityTest {
    @Configuration
    @EnableWebMvc
    @Import({SecurityConfig.class, MisAlertasController.class, MisAlertasServiceImpl.class,
            AlertaClinicaController.class, EtiquetaClinicaController.class, ParametroEvaluacionController.class,
            GlobalExceptionHandler.class})
    static class Config {
        @Bean JwtDecoder jwtDecoder() { return mock(JwtDecoder.class); }
        @Bean UsuarioRepository usuarios() { return mock(UsuarioRepository.class); }
        @Bean UsuarioRegistradoFilter usuarioRegistradoFilter(UsuarioRepository usuarios) { return new UsuarioRegistradoFilter(usuarios); }
        @Bean SeguridadService seguridad() { return mock(SeguridadService.class); }
        @Bean AlertaClinicaRepository alertas() { return mock(AlertaClinicaRepository.class); }
        @Bean AlertaClinicaMapper mapper() { return mock(AlertaClinicaMapper.class); }
        @Bean AlertaClinicaService administracion() { return mock(AlertaClinicaService.class); }
        @Bean AsociacionClinicaService asociaciones() { return mock(AsociacionClinicaService.class); }
        @Bean EtiquetaClinicaService etiquetas() { return mock(EtiquetaClinicaService.class); }
        @Bean ConsultaEtiquetaService consultas() { return mock(ConsultaEtiquetaService.class); }
        @Bean ParametroEvaluacionRepository parametros() { return mock(ParametroEvaluacionRepository.class); }
    }
    @Autowired WebApplicationContext context;
    @Autowired JwtDecoder decoder;
    @Autowired UsuarioRepository usuarios;
    @Autowired SeguridadService seguridad;
    @Autowired AlertaClinicaRepository alertas;
    @Autowired AlertaClinicaMapper mapper;
    @Autowired AlertaClinicaService administracion;
    @Autowired AsociacionClinicaService asociaciones;
    MockMvc mvc;

    @BeforeEach
    void preparar() {
        reset(decoder, usuarios, seguridad, alertas, mapper, administracion, asociaciones);
        when(decoder.decode(anyString())).thenAnswer(inv -> Jwt.withTokenValue(inv.getArgument(0))
                .header("alg", "RS256").subject("demo").claim("email", "demo@example.test")
                .claim("realm_access", Map.of("roles", List.of(inv.getArgument(0, String.class)))).build());
        when(usuarios.existsByEmail("demo@example.test")).thenReturn(true);
        when(seguridad.obtenerMiPerfil()).thenReturn(perfil(7L));
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain", jakarta.servlet.Filter.class)).build();
    }
    private UsuarioPerfilDTO perfil(Long persona) {
        return new UsuarioPerfilDTO(1L, persona, "demo@example.test", "Demo", "TEST", null, true, null, List.of("Colaborador"));
    }

    @Test
    void exigeAutenticacion() throws Exception {
        mvc.perform(get("/api/v1/mis-alertas")).andExpect(status().isUnauthorized());
        verifyNoInteractions(seguridad, alertas);
    }

    @Test
    void listadoIgnoraPersonaManipuladaYConsultaLaPersonaDeSesion() throws Exception {
        when(alertas.buscar(any(), any(), eq(7L), any(), any(), any(), any())).thenReturn(List.of());
        when(mapper.toResponseList(List.of())).thenReturn(List.of());
        mvc.perform(get("/api/v1/mis-alertas").param("personaId", "999").header("Authorization", "Bearer Colaborador"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        verify(alertas).buscar(isNull(), isNull(), eq(7L), isNull(), isNull(), isNull(), any());
        verifyNoInteractions(administracion);
    }

    @Test
    void alertaAjenaResponde404SinMapearNiConsultarPorIdSolo() throws Exception {
        when(alertas.findByIdAlertaAndPersonaId(42L, 7L)).thenReturn(Optional.empty());
        mvc.perform(get("/api/v1/mis-alertas/42").header("Authorization", "Bearer Colaborador"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Alerta no encontrada"));
        verify(alertas).findByIdAlertaAndPersonaId(42L, 7L);
        verify(alertas, never()).findById(any());
        verifyNoInteractions(mapper);
    }

    @Test
    void alertaPropiaIncluyeEtiquetasHistoricas() throws Exception {
        var entidad = new AlertaClinica(); entidad.setIdAlerta(42L); entidad.setPersonaId(7L);
        when(alertas.findByIdAlertaAndPersonaId(42L, 7L)).thenReturn(Optional.of(entidad));
        when(mapper.toResponse(entidad)).thenReturn(AlertaClinicaResponse.builder().idAlerta(42L).personaId(7L)
                .etiquetas(List.of(new EtiquetaAsignadaDto(1L, "RIESGO", "Nombre historico", "#123456", "MOTOR", null))).build());
        mvc.perform(get("/api/v1/mis-alertas/42").header("Authorization", "Bearer Colaborador"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.etiquetas[0].nombre").value("Nombre historico"));
    }

    @Test
    void sinPersonaVinculadaNoEjecutaConsultaSinFiltro() throws Exception {
        when(seguridad.obtenerMiPerfil()).thenReturn(perfil(null));
        mvc.perform(get("/api/v1/mis-alertas").header("Authorization", "Bearer Colaborador")).andExpect(status().isForbidden());
        verifyNoInteractions(alertas);
    }

    @Test
    void colaboradorNoAccedeABandejaCatalogosNiReportesGlobales() throws Exception {
        for (String ruta : List.of("/api/v1/alertas", "/api/v1/alertas/42", "/api/v1/alertas/42/etiquetas",
                "/api/v1/alertas/42/etiquetas/edicion", "/api/v1/alertas/resumen", "/api/v1/alertas/resumen/etiquetas",
                "/api/v1/etiquetas-clinicas", "/api/v1/etiquetas-clinicas/1", "/api/v1/etiquetas-clinicas/reporte",
                "/api/v1/parametros-evaluacion", "/api/v1/parametros-evaluacion/1/etiquetas")) {
            mvc.perform(get(ruta).header("Authorization", "Bearer Colaborador")).andExpect(status().isForbidden());
        }
        verifyNoInteractions(administracion, asociaciones);
    }

    @Test
    void colaboradorNoPuedeModificarAsociacionesNiAtenderOAnular() throws Exception {
        for (String ruta : List.of("/api/v1/alertas/42/etiquetas", "/api/v1/etiquetas-clinicas/1/alertas",
                "/api/v1/etiquetas-clinicas/1/parametros", "/api/v1/parametros-evaluacion/1/etiquetas")) {
            mvc.perform(put(ruta).header("Authorization", "Bearer Colaborador").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"revision\":\"r\",\"detalles\":[]}")).andExpect(status().isForbidden());
        }
        mvc.perform(post("/api/v1/alertas/42/etiquetas").header("Authorization", "Bearer Colaborador")
                .contentType(MediaType.APPLICATION_JSON).content("{\"idEtiqueta\":1}")).andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/alertas/42/etiquetas/1").header("Authorization", "Bearer Colaborador")).andExpect(status().isForbidden());
        for (String accion : List.of("atender", "anular")) {
            mvc.perform(patch("/api/v1/alertas/42/" + accion).header("Authorization", "Bearer Colaborador")
                    .contentType(MediaType.APPLICATION_JSON).content("{\"idUsuario\":1}")).andExpect(status().isForbidden());
        }
        verifyNoInteractions(administracion, asociaciones);
    }

    @Test
    void recursoPropioEsSoloLecturaInclusoParaAdministrador() throws Exception {
        mvc.perform(put("/api/v1/mis-alertas/42").header("Authorization", "Bearer Administrador")
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
    }

    @Test
    void rolesAnidadosDeKeycloakPermitenAdministracionYEvaluacion() throws Exception {
        when(asociaciones.guardarEtiquetasDeAlerta(anyLong(), any())).thenReturn(new AlertaEtiquetasResponse(42L, "r2", List.of()));
        mvc.perform(put("/api/v1/alertas/42/etiquetas").header("Authorization", "Bearer Administrador")
                .contentType(MediaType.APPLICATION_JSON).content("{\"revision\":\"r\",\"detalles\":[]}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.revision").value("r2"));
        mvc.perform(get("/api/v1/alertas/42").header("Authorization", "Bearer Evaluador")).andExpect(status().isOk());
        mvc.perform(patch("/api/v1/alertas/42/atender").header("Authorization", "Bearer Evaluador")
                .contentType(MediaType.APPLICATION_JSON).content("{\"idUsuario\":1}")).andExpect(status().isOk());
        mvc.perform(put("/api/v1/alertas/42/etiquetas").header("Authorization", "Bearer Evaluador")
                .contentType(MediaType.APPLICATION_JSON).content("{\"revision\":\"r\",\"detalles\":[]}")).andExpect(status().isForbidden());
    }
}
