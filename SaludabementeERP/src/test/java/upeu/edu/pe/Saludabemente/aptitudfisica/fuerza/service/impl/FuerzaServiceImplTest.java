package upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.entity.EvaluacionAptitud;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.enums.EstadoEvaluacion;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.service.EvaluacionAptitudService;
import upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.dto.request.FuerzaRequest;
import upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.dto.response.FuerzaResponse;
import upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.entity.DetalleFuerzaResistencia;
import upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.mapper.FuerzaMapper;
import upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.repository.FuerzaRepository;
import upeu.edu.pe.Saludabemente.aptitudfisica.util.MetricasAptitudUtil;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FuerzaServiceImplTest {

    private static final Long ID = 1L;
    private static final FuerzaRequest REQUEST =
            new FuerzaRequest(30, 20, new BigDecimal("150.5"));

    @Mock
    private FuerzaRepository fuerzaRepository;
    @Mock
    private FuerzaMapper fuerzaMapper;
    @Mock
    private EvaluacionAptitudService evaluacionService;
    @Mock
    private MetricasAptitudUtil metricas;

    private FuerzaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new FuerzaServiceImpl(
                fuerzaRepository, fuerzaMapper, evaluacionService, metricas);
    }

    @Test
    void actualizar_recalculaDiagnosticosYDisparaCompletitud() {
        EvaluacionAptitud cabecera = cabecera(EstadoEvaluacion.COMPLETA);
        DetalleFuerzaResistencia detalle =
                DetalleFuerzaResistencia.builder().evaluacion(cabecera).build();
        FuerzaResponse esperado = new FuerzaResponse(
                30, 20, new BigDecimal("150.5"),
                "BUENO", "EXCELENTE", "REGULAR", "BUENO");

        when(evaluacionService.buscarOFallar(ID)).thenReturn(cabecera);
        when(fuerzaRepository.findByEvaluacionId(ID)).thenReturn(Optional.of(detalle));
        when(metricas.dxCore(30, "M", 22)).thenReturn("BUENO");
        when(metricas.dxTrenSuperior(20, "M", 22)).thenReturn("EXCELENTE");
        when(metricas.dxTrenInferior(new BigDecimal("150.5"), "M", 22)).thenReturn("REGULAR");
        when(metricas.consolidarFuerza("BUENO", "EXCELENTE", "REGULAR")).thenReturn("BUENO");
        when(fuerzaRepository.save(detalle)).thenReturn(detalle);
        when(fuerzaMapper.toResponse(detalle)).thenReturn(esperado);

        FuerzaResponse respuesta = service.actualizar(ID, REQUEST);

        assertThat(respuesta).isSameAs(esperado);
        assertThat(detalle.getRepeticionesAbdominales()).isEqualTo(30);
        assertThat(detalle.getRepeticionesFlexionBrazo()).isEqualTo(20);
        assertThat(detalle.getSaltoLongitudCm()).isEqualByComparingTo("150.5");
        assertThat(detalle.getDxCore()).isEqualTo("BUENO");
        assertThat(detalle.getDxTrenSuperior()).isEqualTo("EXCELENTE");
        assertThat(detalle.getDxTrenInferior()).isEqualTo("REGULAR");
        assertThat(detalle.getDxFuerzaGlobal()).isEqualTo("BUENO");
        verify(evaluacionService).evaluarCompletitud(ID);
    }

    @Test
    void actualizar_rechazaEvaluacionAnulada() {
        EvaluacionAptitud cabecera = cabecera(EstadoEvaluacion.ANULADA);
        when(evaluacionService.buscarOFallar(ID)).thenReturn(cabecera);

        assertThatThrownBy(() -> service.actualizar(ID, REQUEST))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ANULADA");

        verify(fuerzaRepository, never()).save(any());
        verify(evaluacionService, never()).evaluarCompletitud(any());
    }

    @Test
    void actualizar_fallaSiNoExisteDetalle() {
        EvaluacionAptitud cabecera = cabecera(EstadoEvaluacion.PENDIENTE);
        when(evaluacionService.buscarOFallar(ID)).thenReturn(cabecera);
        when(fuerzaRepository.findByEvaluacionId(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar(ID, REQUEST))
                .isInstanceOf(upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException.class);

        verify(fuerzaRepository, never()).save(any());
    }

    private EvaluacionAptitud cabecera(EstadoEvaluacion estado) {
        EvaluacionAptitud cabecera = new EvaluacionAptitud();
        cabecera.setId(ID);
        cabecera.setSexoAlMomento("M");
        cabecera.setEdadAlMomento(22);
        cabecera.setEstadoEvaluacion(estado);
        return cabecera;
    }
}
