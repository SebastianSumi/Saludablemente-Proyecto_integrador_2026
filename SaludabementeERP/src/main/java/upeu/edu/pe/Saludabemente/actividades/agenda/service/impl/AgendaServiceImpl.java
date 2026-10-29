package upeu.edu.pe.Saludabemente.actividades.agenda.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.actividades.agenda.dto.request.ActividadRequest;
import upeu.edu.pe.Saludabemente.actividades.agenda.dto.request.SedeRequest;
import upeu.edu.pe.Saludabemente.actividades.agenda.dto.response.ActividadDetalleResponse;
import upeu.edu.pe.Saludabemente.actividades.agenda.dto.response.ActividadResponse;
import upeu.edu.pe.Saludabemente.actividades.agenda.dto.response.CalendarioResponse;
import upeu.edu.pe.Saludabemente.actividades.agenda.entity.Actividad;
import upeu.edu.pe.Saludabemente.actividades.agenda.repository.ActividadRepository;
import upeu.edu.pe.Saludabemente.actividades.agenda.service.AgendaService;
import upeu.edu.pe.Saludabemente.actividades.configuracion.dto.response.ConfiguracionResponse;
import upeu.edu.pe.Saludabemente.actividades.configuracion.service.ConfiguracionService;
import upeu.edu.pe.Saludabemente.actividades.imagen.service.ImagenService;
import upeu.edu.pe.Saludabemente.actividades.inscripcion.dto.response.InscritoDetalleResponse;
import upeu.edu.pe.Saludabemente.actividades.inscripcion.service.InscripcionService;
import upeu.edu.pe.Saludabemente.actividades.integracion.client.SeguridadClientService;
import upeu.edu.pe.Saludabemente.actividades.integracion.dto.CoordinadorSnapshotDto;
import upeu.edu.pe.Saludabemente.actividades.integracion.dto.UsuarioActividadDto;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

import upeu.edu.pe.Saludabemente.actividades.imagen.dto.response.ImagenResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendaServiceImpl implements AgendaService {

    private final ActividadRepository actividadRepository;
    private final SeguridadClientService seguridadClient;
    private final ConfiguracionService configuracionService;
    private final InscripcionService inscripcionService;


    private final ImagenService imagenService;
    private final upeu.edu.pe.Saludabemente.actividades.integracion.client.ActividadSocialSyncService socialSync;

    private static final int TOLERANCIA_GEOCERCA_DEFAULT = 100;

    // ============================================================
    // Crear actividad
    // ============================================================
    @Override
    @Transactional
    public ActividadResponse crearActividadIndividual(ActividadRequest request) {
        socialSync.validarEquipo(request.idEquipoActividad());

        // 1. Validar coordinador
        seguridadClient.validarCoordinador(request.idUsuarioCoordinador());

        // 2. Validar solapamiento
        validarSolapamientoFisico(request.lugar(), request.fechaActividad(),
                request.horaInicio(), request.horaFin());
        validarSolapamientoCoordinador(request.idUsuarioCoordinador(),
                request.fechaActividad(),
                request.horaInicio(), request.horaFin());

        // 3. Crear entidad
        Actividad a = new Actividad();
        a.setNombre(request.nombre());
        a.setDescripcion(request.descripcion());
        a.setFechaActividad(request.fechaActividad());
        a.setHoraInicio(request.horaInicio());
        a.setHoraFin(request.horaFin());
        a.setLugar(request.lugar());
        a.setEstado("PROGRAMADA");
        a.setIdUsuarioCreador(request.idUsuarioCreador());
        a.setIdEquipoActividad(request.idEquipoActividad());
        a.setIdUsuarioCoordinador(request.idUsuarioCoordinador());

        // 4. Campos de Asistencias (con defaults)
        a.setLatitudOficial(request.latitudOficial());
        a.setLongitudOficial(request.longitudOficial());
        a.setToleranciaGeocercaMetros(
                request.toleranciaGeocercaMetros() != null
                        ? request.toleranciaGeocercaMetros()
                        : TOLERANCIA_GEOCERCA_DEFAULT);
        a.setRequiereCheckInOut(
                request.requiereCheckInOut() != null
                        ? request.requiereCheckInOut()
                        : false);
        a.setDuracionMinutos(request.duracionMinutos());
        a.setPeriodo(
                request.periodo() != null && !request.periodo().isBlank()
                        ? request.periodo()
                        : derivarPeriodo(request.fechaActividad()));
        a.setPermiteAutoservicio(
                request.permiteAutoservicio() != null
                        ? request.permiteAutoservicio()
                        : true);
        a.setPermiteEscaneoCoordinador(
                request.permiteEscaneoCoordinador() != null
                        ? request.permiteEscaneoCoordinador()
                        : true);

        // 5. Campo interno para Asistencias
        a.setCierreAsistenciasRealizado(false);

        // 6. Guardar
        Actividad guardada = actividadRepository.save(a);
        socialSync.sincronizar(guardada.getIdActividad());
        return toResponse(guardada);
    }

    // ============================================================
    // Consultas
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public CalendarioResponse listarParaCalendario(int mes, int anio) {
        YearMonth ym = YearMonth.of(anio, mes);
        LocalDate inicio = ym.atDay(1);
        LocalDate fin = ym.atEndOfMonth();

        List<ActividadResponse> actividades = actividadRepository
                .findBetweenDates(inicio, fin)
                .stream()
                .map(this::toResponse)
                .toList();

        return new CalendarioResponse(mes, anio, actividades);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActividadResponse> obtenerActivasDelDia() {
        return actividadRepository.findActivasDelDia(LocalDate.now())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ActividadDetalleResponse obtenerPorId(Long id) {
        Actividad a = actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada: " + id));

        UsuarioActividadDto creador = seguridadClient.obtenerUsuario(a.getIdUsuarioCreador());

        // 👇 TOLERANTE: si el coordinador ya no existe, no revienta el detalle
        CoordinadorSnapshotDto coordinador =
                seguridadClient.obtenerCoordinadorOpcional(a.getIdUsuarioCoordinador());
        String nombreCoordinador = coordinador != null
                ? coordinador.nombreCompleto()
                : "Sin asignar";

        ConfiguracionResponse cfg = configuracionService.obtenerConfiguracion(id);
        List<InscritoDetalleResponse> inscritos = inscripcionService.listarInscritosDetalle(id);

        String logoUrl = a.getLogoUrl();
        List<ImagenResponse> imagenes = imagenService.listarImagenes(id);

        return new ActividadDetalleResponse(
                a.getIdActividad(),
                a.getNombre(),
                a.getDescripcion(),
                a.getFechaActividad(),
                a.getHoraInicio(),
                a.getHoraFin(),
                a.getLugar(),
                a.getEstado(),
                a.getPuntajeSalud(),
                a.getPeriodo(),
                a.getIdUsuarioCreador(),
                creador.nombreCompleto(),
                creador.email(),
                creador.rol(),
                a.getFechaCreacion(),
                a.getIdUsuarioCoordinador(),
                nombreCoordinador,
                a.getLatitudOficial(),
                a.getLongitudOficial(),
                a.getToleranciaGeocercaMetros(),
                a.getRequiereCheckInOut(),
                a.getDuracionMinutos(),
                a.getPermiteAutoservicio(),
                a.getPermiteEscaneoCoordinador(),
                cfg.materiales(),
                inscritos,
                (long) inscritos.size(),
                logoUrl,
                imagenes
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActividadResponse> listarTodas() {
        return actividadRepository.findAll().stream().map(this::toResponse).toList();
    }

    // ============================================================
    // Actualizar sede (geocerca por actividad)
    // ============================================================
    @Override
    @Transactional
    public ActividadResponse actualizarSede(Long id, SedeRequest request) {
        Actividad a = actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada: " + id));
        if ("FINALIZADA".equals(a.getEstado()) || "CANCELADA".equals(a.getEstado())) {
            throw new BusinessRuleException("No se puede mover la sede de una actividad " + a.getEstado());
        }
        a.setLatitudOficial(request.latitudOficial());
        a.setLongitudOficial(request.longitudOficial());
        a.setToleranciaGeocercaMetros(
                request.toleranciaGeocercaMetros() != null
                        ? request.toleranciaGeocercaMetros()
                        : TOLERANCIA_GEOCERCA_DEFAULT);
        return toResponse(actividadRepository.save(a));
    }

    // ============================================================
    // Helpers
    // ============================================================
    private void validarSolapamientoFisico(String lugar, LocalDate fecha,
                                           LocalTime inicio, LocalTime fin) {
        long conflictos = actividadRepository.countConflictoEspacio(lugar, fecha, inicio, fin);
        if (conflictos > 0) {
            throw new BusinessRuleException(
                    "Ya existe una actividad programada en " + lugar + " en ese horario");
        }
    }

    private void validarSolapamientoCoordinador(Long idUsuario, LocalDate fecha,
                                                LocalTime inicio, LocalTime fin) {
        long conflictos = actividadRepository.countConflictoCoordinador(idUsuario, fecha, inicio, fin);
        if (conflictos > 0) {
            throw new BusinessRuleException(
                    "El coordinador ya tiene otra actividad asignada en ese horario");
        }
    }

    /**
     * Deriva el periodo semestral a partir de la fecha.
     * Enero-Junio = "YYYY-I", Julio-Diciembre = "YYYY-II".
     */
    private String derivarPeriodo(LocalDate fecha) {
        int anio = fecha.getYear();
        return fecha.getMonthValue() <= 6 ? anio + "-I" : anio + "-II";
    }

    private ActividadResponse toResponse(Actividad a) {
        // 👇 TOLERANTE
        CoordinadorSnapshotDto coordinador =
                seguridadClient.obtenerCoordinadorOpcional(a.getIdUsuarioCoordinador());
        String nombreCoordinador = coordinador != null
                ? coordinador.nombreCompleto()
                : "Sin asignar";

        return new ActividadResponse(
                a.getIdActividad(),
                a.getNombre(),
                a.getDescripcion(),
                a.getFechaActividad(),
                a.getHoraInicio(),
                a.getHoraFin(),
                a.getLugar(),
                a.getEstado(),
                a.getIdUsuarioCreador(),
                a.getIdUsuarioCoordinador(),
                nombreCoordinador,
                a.getLatitudOficial(),
                a.getLongitudOficial(),
                a.getToleranciaGeocercaMetros(),
                a.getRequiereCheckInOut(),
                a.getDuracionMinutos(),
                a.getPeriodo(),
                a.getPermiteAutoservicio(),
                a.getPermiteEscaneoCoordinador(),
                a.getLogoUrl()                  // 👈 NUEVO
        );
    }
}
