package upeu.edu.pe.Saludabemente.asistencias.exoneracion.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.actividades.api.ActividadApiService;
import upeu.edu.pe.Saludabemente.actividades.api.dto.ActividadSnapshotDto;
import upeu.edu.pe.Saludabemente.asistencias.enums.EstadoAsistencia;
import upeu.edu.pe.Saludabemente.asistencias.enums.EstadoJustificacion;
import upeu.edu.pe.Saludabemente.asistencias.exoneracion.dto.request.DictamenRequest;
import upeu.edu.pe.Saludabemente.asistencias.exoneracion.dto.request.JustificacionRequest;
import upeu.edu.pe.Saludabemente.asistencias.exoneracion.dto.response.JustificacionResponse;
import upeu.edu.pe.Saludabemente.asistencias.exoneracion.entity.Justificacion;
import upeu.edu.pe.Saludabemente.asistencias.exoneracion.repository.JustificacionRepository;
import upeu.edu.pe.Saludabemente.asistencias.exoneracion.service.ExoneracionService;
import upeu.edu.pe.Saludabemente.asistencias.registro.entity.Asistencia;
import upeu.edu.pe.Saludabemente.asistencias.registro.repository.AsistenciaRepository;
import upeu.edu.pe.Saludabemente.asistencias.util.RachaCalculator;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.personal.api.PersonalApiService;
import upeu.edu.pe.Saludabemente.personal.api.PersonalSnapshotDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExoneracionServiceImpl implements ExoneracionService {

    private final JustificacionRepository justificacionRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final PersonalApiService personalApiService;
    private final ActividadApiService actividadApiService;

    // ============================================================
    // SOLICITAR
    // ============================================================
    @Override
    @Transactional
    public JustificacionResponse solicitar(JustificacionRequest request) {

        // 1. Validar persona
        PersonalSnapshotDTO persona = personalApiService.obtenerSnapshot(request.idPersona());
        if (persona == null) {
            throw new ResourceNotFoundException("Persona no encontrada: " + request.idPersona());
        }

        // 2. Validar actividad
        ActividadSnapshotDto actividad = actividadApiService.obtenerSnapshot(request.idActividad());
        if (actividad == null) {
            throw new ResourceNotFoundException("Actividad no encontrada: " + request.idActividad());
        }

        // 3. Debe existir una asistencia AUSENTE para esa persona/actividad
        List<Asistencia> marcas = asistenciaRepository
                .findByIdActividadAndIdPersonaIn(request.idActividad(), List.of(request.idPersona()));

        boolean esAusente = marcas.stream()
                .anyMatch(a -> a.getEstado() == EstadoAsistencia.AUSENTE);

        if (!esAusente) {
            throw new BusinessRuleException(
                    "No hay una ausencia registrada para esta persona en esta actividad");
        }

        // 4. No duplicar solicitud pendiente
        boolean yaExiste = justificacionRepository
                .existsByIdActividadAndIdPersonaAndEstado(
                        request.idActividad(),
                        request.idPersona(),
                        EstadoJustificacion.PENDIENTE);

        if (yaExiste) {
            throw new BusinessRuleException(
                    "Ya existe una solicitud pendiente para esta actividad y persona");
        }

        // 5. Crear
        Justificacion j = Justificacion.builder()
                .idActividad(request.idActividad())
                .idPersona(request.idPersona())
                .motivo(request.motivo())
                .urlDocumento(request.urlDocumento())
                .estado(EstadoJustificacion.PENDIENTE)
                .fechaSolicitud(LocalDateTime.now())
                .build();

        Justificacion guardada = justificacionRepository.save(j);
        log.info("Justificación registrada id={} persona={} actividad={}",
                guardada.getIdJustificacion(), request.idPersona(), request.idActividad());

        return toResponse(guardada);
    }

    // ============================================================
    // DICTAMINAR
    // ============================================================
    @Override
    @Transactional
    public JustificacionResponse dictaminar(Long idJustificacion, DictamenRequest request) {

        Justificacion j = justificacionRepository.findById(idJustificacion)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Justificación no encontrada: " + idJustificacion));

        if (j.getEstado() != EstadoJustificacion.PENDIENTE) {
            throw new BusinessRuleException(
                    "Esta justificación ya fue dictaminada: " + j.getEstado());
        }

        String estado = request.estado().toUpperCase();

        if ("APROBADO".equals(estado)) {
            j.setEstado(EstadoJustificacion.APROBADO);
            mutarAsistenciaAExonerado(j);
        } else if ("RECHAZADO".equals(estado)) {
            j.setEstado(EstadoJustificacion.RECHAZADO);
        } else {
            throw new BusinessRuleException("Estado inválido: " + estado);
        }

        j.setObservaciones(request.observaciones());
        j.setIdUsuarioDictamina(request.idUsuarioDictamina());
        j.setFechaDictamen(LocalDateTime.now());

        Justificacion guardada = justificacionRepository.save(j);
        log.info("Justificación dictaminada id={} estado={}", idJustificacion, estado);

        return toResponse(guardada);
    }

    // ============================================================
    // LISTAR
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public Page<JustificacionResponse> listar(
            EstadoJustificacion estado,
            Long idPersona,
            LocalDateTime desde,
            LocalDateTime hasta,
            Pageable pageable) {
        return justificacionRepository
                .buscarConFiltros(estado, idPersona, desde, hasta, pageable)
                .map(this::toResponse);
    }

    // ============================================================
    // AUSENTISMO CRÓNICO
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public List<Long> detectarAusentismoCronico(int minRacha) {

        List<PersonalSnapshotDTO> activos = personalApiService.listarPersonalActivo();
        List<Long> resultado = new ArrayList<>();

        for (PersonalSnapshotDTO p : activos) {
            List<Asistencia> historial = asistenciaRepository.findByIdPersona(p.idPersona());
            int racha = RachaCalculator.calcularRachaFaltas(historial);
            if (racha >= minRacha) {
                resultado.add(p.idPersona());
                log.info("Ausentismo crónico detectado persona={} racha={}", p.idPersona(), racha);
            }
        }

        return resultado;
    }

    // ============================================================
    // MUTACIÓN AUSENTE → EXONERADO
    // ============================================================
    private void mutarAsistenciaAExonerado(Justificacion j) {

        List<Asistencia> marcas = asistenciaRepository
                .findByIdActividadAndIdPersonaIn(j.getIdActividad(), List.of(j.getIdPersona()));

        Optional<Asistencia> ausente = marcas.stream()
                .filter(a -> a.getEstado() == EstadoAsistencia.AUSENTE)
                .findFirst();

        if (ausente.isEmpty()) {
            log.warn("No se encontró AUSENTE para mutar. Justificación={}", j.getIdJustificacion());
            return;
        }

        Asistencia a = ausente.get();
        a.setEstado(EstadoAsistencia.EXONERADO);
        a.setUpdatedAt(LocalDateTime.now());
        asistenciaRepository.save(a);

        log.info("Asistencia mutada de AUSENTE a EXONERADO id={} persona={} actividad={}",
                a.getIdAsistencia(), a.getIdPersona(), a.getIdActividad());
    }

    // ============================================================
    // MAPPER
    // ============================================================
    private JustificacionResponse toResponse(Justificacion j) {
        return new JustificacionResponse(
                j.getIdJustificacion(),
                j.getIdActividad(),
                j.getIdPersona(),
                j.getMotivo(),
                j.getUrlDocumento(),
                j.getEstado(),
                j.getObservaciones(),
                j.getIdUsuarioDictamina(),
                j.getFechaSolicitud(),
                j.getFechaDictamen()
        );
    }
}