package upeu.edu.pe.Saludabemente.actividades.operacion.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.actividades.agenda.dto.response.ActividadResponse;
import upeu.edu.pe.Saludabemente.actividades.agenda.entity.Actividad;
import upeu.edu.pe.Saludabemente.actividades.agenda.repository.ActividadRepository;
import upeu.edu.pe.Saludabemente.actividades.integracion.client.SeguridadClientService;
import upeu.edu.pe.Saludabemente.actividades.integracion.dto.CoordinadorSnapshotDto;
import upeu.edu.pe.Saludabemente.actividades.operacion.dto.request.CancelacionRequest;
import upeu.edu.pe.Saludabemente.actividades.operacion.dto.request.ReprogramacionRequest;
import upeu.edu.pe.Saludabemente.actividades.operacion.dto.request.TransferenciaRequest;
import upeu.edu.pe.Saludabemente.actividades.operacion.service.OperacionService;
import upeu.edu.pe.Saludabemente.asistencias.qr.dto.response.QrActividadResponse;
import upeu.edu.pe.Saludabemente.asistencias.qr.service.QrActividadService;
import upeu.edu.pe.Saludabemente.asistencias.qr.service.QrStreamService;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;


import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperacionServiceImpl implements OperacionService {

    private final ActividadRepository actividadRepository;
    private final QrActividadService qrActividadService;
    private final QrStreamService qrStreamService;
    private final SeguridadClientService seguridadClient;
    private final upeu.edu.pe.Saludabemente.actividades.integracion.client.ActividadSocialSyncService socialSync;

    @Override
    @Transactional
    public ActividadResponse reprogramar(Long id, ReprogramacionRequest req) {
        Actividad a = obtenerActividad(id);
        if (!"PROGRAMADA".equals(a.getEstado())) {
            throw new BusinessRuleException("Solo se puede reprogramar una actividad PROGRAMADA");
        }
        validarSolapamiento(a.getLugar(), req.nuevaFecha(),
                req.nuevaHoraInicio(), req.nuevaHoraFin(), id);
        validarSolapamientoCoordinador(a.getIdUsuarioCoordinador(), req.nuevaFecha(),
                req.nuevaHoraInicio(), req.nuevaHoraFin(), id);

        a.setFechaActividad(req.nuevaFecha());
        a.setHoraInicio(req.nuevaHoraInicio());
        a.setHoraFin(req.nuevaHoraFin());
        actividadRepository.save(a);
        socialSync.sincronizar(id);
        return toResponse(actividadRepository.save(a));
    }

    @Override
    @Transactional
    public ActividadResponse cancelar(Long id, CancelacionRequest req) {
        Actividad a = obtenerActividad(id);
        if ("FINALIZADA".equals(a.getEstado()) || "CANCELADA".equals(a.getEstado())) {
            throw new BusinessRuleException("No se puede cancelar una actividad " + a.getEstado());
        }
        a.setEstado("CANCELADA");
        actividadRepository.save(a);
        socialSync.sincronizar(id);

        return toResponse(actividadRepository.save(a));
    }

    @Override
    @Transactional
    public ActividadResponse iniciar(Long id) {
        Actividad a = obtenerActividad(id);
        if (!"PROGRAMADA".equals(a.getEstado())) {
            throw new BusinessRuleException("Solo se puede iniciar una actividad PROGRAMADA");
        }
        a.setEstado("EN_CURSO");
        Actividad guardada = actividadRepository.save(a);
        socialSync.sincronizar(id);
        // QR único (sistema QrActividad): autogenerado al iniciar para que exista desde el segundo 0.
        try {
            QrActividadResponse qr = qrActividadService.rotar(guardada.getIdActividad());
            qrStreamService.emitir(guardada.getIdActividad(), qr);
        } catch (Exception e) {
            log.warn("No se pudo generar QR inicial actividad={}: {}", guardada.getIdActividad(), e.getMessage());
        }
        return toResponse(guardada);
    }

    @Override
    @Transactional
    public ActividadResponse finalizar(Long id) {
        Actividad a = obtenerActividad(id);
        if (!"EN_CURSO".equals(a.getEstado())) {
            throw new BusinessRuleException("Solo se puede finalizar una actividad EN_CURSO");
        }
        a.setEstado("FINALIZADA");
        actividadRepository.save(a);
        socialSync.sincronizar(id);
        qrStreamService.cerrarTodos(id);
        return toResponse(actividadRepository.save(a));
    }

    @Override
    @Transactional
    public ActividadResponse transferirCoordinador(Long id, TransferenciaRequest req) {
        Actividad a = obtenerActividad(id);
        seguridadClient.validarCoordinador(req.idNuevoCoordinador());
        a.setIdUsuarioCoordinador(req.idNuevoCoordinador());
        actividadRepository.save(a);
        socialSync.sincronizar(id);
        return toResponse(actividadRepository.save(a));
    }

    // ===================== helpers =====================

    private Actividad obtenerActividad(Long id) {
        return actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada: " + id));
    }

    private void validarSolapamiento(String lugar, LocalDate fecha,
                                     LocalTime inicio, LocalTime fin, Long idExcluir) {
        long c = actividadRepository.countConflictoEspacioExcluyendo(lugar, fecha, inicio, fin, idExcluir);
        if (c > 0) throw new BusinessRuleException("Conflicto de lugar/horario");
    }

    private void validarSolapamientoCoordinador(Long idUsuario, LocalDate fecha,
                                                LocalTime inicio, LocalTime fin, Long idExcluir) {
        long c = actividadRepository.countConflictoCoordinadorExcluyendo(idUsuario, fecha, inicio, fin, idExcluir);
        if (c > 0) throw new BusinessRuleException("Conflicto de coordinador");
    }

    private ActividadResponse toResponse(Actividad a) {
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
                a.getLogoUrl()                   // 👈 NUEVO
        );
    }
}
