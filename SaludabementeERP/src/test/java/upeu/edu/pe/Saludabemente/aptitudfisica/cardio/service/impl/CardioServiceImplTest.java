package upeu.edu.pe.Saludabemente.aptitudfisica.cardio.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import upeu.edu.pe.Saludabemente.aptitudfisica.cardio.dto.request.CardioRequest;
import upeu.edu.pe.Saludabemente.aptitudfisica.cardio.dto.response.CardioResponse;
import upeu.edu.pe.Saludabemente.aptitudfisica.cardio.entity.DetalleCardiovascular;
import upeu.edu.pe.Saludabemente.aptitudfisica.cardio.mapper.CardioMapper;
import upeu.edu.pe.Saludabemente.aptitudfisica.cardio.repository.CardioRepository;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.entity.EvaluacionAptitud;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.enums.EstadoEvaluacion;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.service.EvaluacionAptitudService;
import upeu.edu.pe.Saludabemente.aptitudfisica.util.MetricasAptitudUtil;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardioServiceImplTest {

    private static final Long ID = 1L;
    private static final CardioRequest REQUEST = new CardioRequest(80, 90);

    @Mock
    private CardioRepository cardioRepository;
    @Mock
    private CardioMapper cardioMapper;
    @Mock
    private EvaluacionAptitudService evaluacionService;
    @Mock
    private MetricasAptitudUtil metricas;

    private CardioServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CardioServiceImpl(
                cardioRepository, cardioMapper, evaluacionService, metricas);
    }

    @Test
    void actualizar_recalculaDiagnosticoYDisparaCompletitud() {
        EvaluacionAptitud cabecera = cabecera(EstadoEvaluacion.COMPLETA);
        DetalleCardiovascular detalle =
                DetalleCardiovascular.builder().evaluacion(cabecera).build();
        CardioResponse esperado = new CardioResponse(80, 90, "EXCELENTE");

        when(evaluacionService.buscarOFallar(ID)).thenReturn(cabecera);
        when(cardioRepository.findByEvaluacionId(ID)).thenReturn(Optional.of(detalle));
        when(metricas.dxCapacidadCardio(80, 90, "F", 25)).thenReturn("EXCELENTE");
        when(cardioRepository.save(detalle)).thenReturn(detalle);
        when(cardioMapper.toResponse(detalle)).thenReturn(esperado);

        CardioResponse respuesta = service.actualizar(ID, REQUEST);

        assertThat(respuesta).isSameAs(esperado);
        assertThat(detalle.getRepeticionesSoga30s()).isEqualTo(80);
        assertThat(detalle.getTiempoCarrera400mSeg()).isEqualTo(90);
        assertThat(detalle.getDxCardiovascular()).isEqualTo("EXCELENTE");
        verify(evaluacionService).evaluarCompletitud(ID);
    }

    @Test
    void actualizar_rechazaEvaluacionAnulada() {
        EvaluacionAptitud cabecera = cabecera(EstadoEvaluacion.ANULADA);
        when(evaluacionService.buscarOFallar(ID)).thenReturn(cabecera);

        assertThatThrownBy(() -> service.actualizar(ID, REQUEST))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ANULADA");

        verify(cardioRepository, never()).save(any());
        verify(evaluacionService, never()).evaluarCompletitud(any());
    }

    @Test
    void actualizar_fallaSiNoExisteDetalle() {
        EvaluacionAptitud cabecera = cabecera(EstadoEvaluacion.PENDIENTE);
        when(evaluacionService.buscarOFallar(ID)).thenReturn(cabecera);
        when(cardioRepository.findByEvaluacionId(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar(ID, REQUEST))
                .isInstanceOf(upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException.class);

        verify(cardioRepository, never()).save(any());
    }

    private EvaluacionAptitud cabecera(EstadoEvaluacion estado) {
        EvaluacionAptitud cabecera = new EvaluacionAptitud();
        cabecera.setId(ID);
        cabecera.setSexoAlMomento("F");
        cabecera.setEdadAlMomento(25);
        cabecera.setEstadoEvaluacion(estado);
        return cabecera;
    }
}
