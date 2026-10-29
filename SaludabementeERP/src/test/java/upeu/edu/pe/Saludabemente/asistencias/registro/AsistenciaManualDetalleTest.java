package upeu.edu.pe.Saludabemente.asistencias.registro;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import upeu.edu.pe.Saludabemente.actividades.api.ActividadApiService;
import upeu.edu.pe.Saludabemente.actividades.api.dto.ActividadSnapshotDto;
import upeu.edu.pe.Saludabemente.asistencias.enums.*;
import upeu.edu.pe.Saludabemente.asistencias.qr.service.QrActividadService;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.request.AsistenciaManualDetalleRequest;
import upeu.edu.pe.Saludabemente.asistencias.registro.entity.Asistencia;
import upeu.edu.pe.Saludabemente.asistencias.registro.mapper.AsistenciaMapper;
import upeu.edu.pe.Saludabemente.asistencias.registro.repository.AsistenciaRepository;
import upeu.edu.pe.Saludabemente.asistencias.registro.service.impl.AsistenciaServiceImpl;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.personal.api.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AsistenciaManualDetalleTest {
    private final AsistenciaRepository repo = mock(AsistenciaRepository.class);
    private final ActividadApiService actividades = mock(ActividadApiService.class);
    private final PersonalApiService personal = mock(PersonalApiService.class);
    private final AsistenciaMapper mapper = mock(AsistenciaMapper.class);
    private final AsistenciaServiceImpl service = new AsistenciaServiceImpl(repo, mapper, personal,
            mock(CredencialApiService.class), actividades, mock(QrActividadService.class));

    private ActividadSnapshotDto actividad(String estado) {
        return new ActividadSnapshotDto(8L, "Actividad", null, estado, null, null, null,
                null, null, null, null, false, null, null, null, true, true);
    }

    @BeforeEach
    void preparar() {
        when(actividades.obtenerSnapshot(8L)).thenReturn(actividad("EN_CURSO"));
    }

    private AsistenciaManualDetalleRequest request(AsistenciaManualDetalleRequest.Detalle... detalle) {
        return new AsistenciaManualDetalleRequest(8L, TipoAsistencia.SIMPLE, null, null, List.of(detalle));
    }

    @Test
    void rechazaPersonasRepetidasAntesDeGuardar() {
        var item = new AsistenciaManualDetalleRequest.Detalle(20L, MarcaAsistencia.PRESENTE);
        assertThatThrownBy(() -> service.guardarManualDetalle(request(item, item)))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("repetida");
        verifyNoInteractions(repo);
    }

    @Test
    void rechazaEdicionFueraDeActividadEnCurso() {
        when(actividades.obtenerSnapshot(8L)).thenReturn(actividad("FINALIZADA"));
        assertThatThrownBy(() -> service.guardarManualDetalle(request(
                new AsistenciaManualDetalleRequest.Detalle(20L, MarcaAsistencia.TARDE))))
                .isInstanceOf(BusinessRuleException.class);
        verifyNoInteractions(repo);
    }

    @Test
    void creaYEditaMarcasMixtasConservandoElIdExistente() {
        for (long id : List.of(20L, 21L)) {
            when(personal.obtenerSnapshot(id)).thenReturn(new PersonalSnapshotDTO(id, "Persona", true,
                    null, null, null, null, null, null, null));
            when(actividades.estaInscrito(8L, id)).thenReturn(true);
        }
        var existente = Asistencia.builder().idAsistencia(101L).idActividad(8L)
                .idPersona(20L).tipoAsistencia(TipoAsistencia.SIMPLE).build();
        when(repo.findByIdActividadAndIdPersonaAndTipoAsistencia(8L, 20L, TipoAsistencia.SIMPLE))
                .thenReturn(Optional.of(existente));
        when(repo.findByIdActividadAndIdPersonaAndTipoAsistencia(8L, 21L, TipoAsistencia.SIMPLE))
                .thenReturn(Optional.empty());
        when(repo.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        var result = service.guardarManualDetalle(request(
                new AsistenciaManualDetalleRequest.Detalle(20L, MarcaAsistencia.TARDE),
                new AsistenciaManualDetalleRequest.Detalle(21L, MarcaAsistencia.FALTA)));
        assertThat(existente.getIdAsistencia()).isEqualTo(101L);
        assertThat(existente.getEstado()).isEqualTo(EstadoAsistencia.TARDE);
        verify(repo).saveAndFlush(argThat(a -> a.getIdPersona().equals(21L)
                && a.getEstado() == EstadoAsistencia.AUSENTE && a.getIdAsistencia() == null));
        assertThat(result.idActividad()).isEqualTo(8L);
    }

    @Test
    void consultaActividadSinAsistenciasDevuelveDetalleVacio() {
        when(repo.findByIdActividad(8L)).thenReturn(List.of());
        when(mapper.toResponseList(List.of())).thenReturn(List.of());
        var result = service.consultarDetalle(8L);
        assertThat(result.nombreActividad()).isEqualTo("Actividad");
        assertThat(result.detalle()).isEmpty();
        verify(repo, never()).saveAndFlush(any());
    }
}
