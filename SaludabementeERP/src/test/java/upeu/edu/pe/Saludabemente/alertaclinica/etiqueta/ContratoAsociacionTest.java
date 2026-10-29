package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta;

import io.swagger.v3.core.converter.ModelConverters;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.controller.EtiquetaClinicaController;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.*;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.service.*;
import upeu.edu.pe.Saludabemente.exception.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ContratoAsociacionTest {
    @Test
    void jsonCompuestoSeRecibeYDevuelveDesdeElControlador() throws Exception {
        var asociaciones = mock(AsociacionClinicaService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new EtiquetaClinicaController(asociaciones,
                mock(ConsultaEtiquetaService.class), mock(EtiquetaClinicaService.class)))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        var request = new GuardarParametrosRequest("revision", List.of(new ParametroDetalleRequest(2L)));
        when(asociaciones.guardarParametrosDeEtiqueta(1L, request)).thenReturn(
                new EtiquetaParametrosResponse(1L, "nueva", List.of(new ParametroFotocopiaDto(2L, "IMC", "Indice", "kg/m2"))));
        mvc.perform(put("/api/v1/etiquetas-clinicas/1/parametros").contentType(MediaType.APPLICATION_JSON)
                .content("{\"revision\":\"revision\",\"detalles\":[{\"idParametro\":2}]}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.revision").value("nueva"))
                .andExpect(jsonPath("$.detalles[0].idParametro").value(2));
        verify(asociaciones).guardarParametrosDeEtiqueta(1L, request);
    }

    @Test
    void validacionDeCadaDetalleResponde400AntesDelServicio() throws Exception {
        var asociaciones = mock(AsociacionClinicaService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new EtiquetaClinicaController(asociaciones,
                mock(ConsultaEtiquetaService.class), mock(EtiquetaClinicaService.class)))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        for (String cuerpo : List.of("{\"revision\":\"r\",\"detalles\":[{\"idParametro\":0}]}",
                "{\"revision\":\"r\",\"detalles\":[null]}", "{\"revision\":\"r\"}",
                "{\"detalles\":[]}")) {
            mvc.perform(put("/api/v1/etiquetas-clinicas/1/parametros").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(asociaciones);
    }

    @Test
    void enumDeConsultaInvalidoResponde400() throws Exception {
        var consultas = mock(ConsultaEtiquetaService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new EtiquetaClinicaController(mock(AsociacionClinicaService.class),
                consultas, mock(EtiquetaClinicaService.class)))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get("/api/v1/etiquetas-clinicas/reporte").param("severidad", "INVALIDA"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Valor invalido para el parametro: severidad"));
        verifyNoInteractions(consultas);
    }

    @Test
    void swaggerExponeColeccionDeObjetosEnLugarDeListaDeNumeros() {
        var schemas = ModelConverters.getInstance().read(GuardarEtiquetasRequest.class);
        var schema = schemas.get("GuardarEtiquetasRequest");
        assertThat(schema.getRequired()).contains("revision", "detalles");
        var detalles = (io.swagger.v3.oas.models.media.Schema<?>) schema.getProperties().get("detalles");
        assertThat(detalles.getItems().get$ref()).endsWith("/EtiquetaDetalleRequest");
        var etiqueta = ModelConverters.getInstance().read(EtiquetaClinicaRequestDto.class).get("EtiquetaClinicaRequestDto");
        assertThat(etiqueta.getProperties()).containsKey("detalles").doesNotContainKey("idsParametros");
    }
}
