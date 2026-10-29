package upeu.edu.pe.Saludabemente.asistencias.qr.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.actividades.api.ActividadApiService;
import upeu.edu.pe.Saludabemente.actividades.api.dto.ActividadSnapshotDto;
import upeu.edu.pe.Saludabemente.asistencias.qr.config.QrActividadProperties;
import upeu.edu.pe.Saludabemente.asistencias.qr.dto.response.QrActividadResponse;
import upeu.edu.pe.Saludabemente.asistencias.qr.entity.QrActividad;
import upeu.edu.pe.Saludabemente.asistencias.qr.repository.QrActividadRepository;
import upeu.edu.pe.Saludabemente.asistencias.qr.service.QrActividadService;
import upeu.edu.pe.Saludabemente.asistencias.util.ConstantesAsistencia;
import upeu.edu.pe.Saludabemente.asistencias.util.QrActividadUtil;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class QrActividadServiceImpl implements QrActividadService {

    private final QrActividadRepository qrActividadRepository;
    private final QrActividadProperties properties;
    private final ActividadApiService actividadApiService;

    @Override
    @Transactional
    public QrActividadResponse rotar(Long idActividad) {

        ActividadSnapshotDto actividad = actividadApiService.obtenerSnapshot(idActividad);
        if (actividad == null) {
            throw new ResourceNotFoundException("Actividad no encontrada: " + idActividad);
        }
        if (ConstantesAsistencia.ACTIVIDAD_CANCELADA.equals(actividad.estado())) {
            throw new BusinessRuleException("No se puede generar QR para actividad cancelada");
        }
        if (ConstantesAsistencia.ACTIVIDAD_FINALIZADA.equals(actividad.estado())) {
            throw new BusinessRuleException("No se puede generar QR para actividad finalizada");
        }

        qrActividadRepository.desactivarTodosByIdActividad(idActividad);

        Integer versionActual = qrActividadRepository
                .findTopByIdActividadOrderByVersionDesc(idActividad)
                .map(QrActividad::getVersion)
                .orElse(0);
        int nuevaVersion = versionActual + 1;

        long ahora = Instant.now().toEpochMilli();
        String codigoQr = QrActividadUtil.construir(idActividad, nuevaVersion, ahora);

        LocalDateTime ahoraLdt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(ahora), ZoneId.systemDefault());
        LocalDateTime ventanaInicio = ahoraLdt.minusMinutes(properties.getVentanaAnticipacionMin());
        LocalDateTime ventanaFin = ahoraLdt.plusSeconds(properties.getRotacionSegundos());

        QrActividad qr = QrActividad.builder()
                .idActividad(idActividad)
                .version(nuevaVersion)
                .codigoQr(codigoQr)
                .ventanaInicio(ventanaInicio)
                .ventanaFin(ventanaFin)
                .activo(true)
                .generadoEn(ahoraLdt)
                .build();

        QrActividad guardado = qrActividadRepository.save(qr);
        log.info("QR rotado actividad={} version={}", idActividad, nuevaVersion);

        return toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public QrActividadResponse obtenerActual(Long idActividad) {
        QrActividad qr = qrActividadRepository
                .findByActivoTrueAndIdActividad(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No hay QR vigente para la actividad: " + idActividad));
        return toResponse(qr);
    }

    @Override
    @Transactional(readOnly = true)
    public void validarCodigoQr(String codigoQr, LocalDateTime momentoReferencia) {

        QrActividadUtil.ParseResult parse = QrActividadUtil.parsear(codigoQr);
        if (parse == null) {
            throw new BusinessRuleException("QR con formato inválido");
        }

        QrActividad qr = qrActividadRepository.findByCodigoQr(codigoQr)
                .orElseThrow(() -> new BusinessRuleException("QR inexistente"));

        if (parse.idActividad() == null || !parse.idActividad().equals(qr.getIdActividad())) {
            throw new BusinessRuleException("QR de otra actividad");
        }

        LocalDateTime ref = momentoReferencia != null ? momentoReferencia : LocalDateTime.now();

        // 1. Vigente: activo, misma versión y dentro de ventana (+ tolerancia de reloj).
        boolean vigente = Boolean.TRUE.equals(qr.getActivo())
                && qr.getVersion() != null && qr.getVersion().equals(parse.version())
                && (qr.getVentanaFin() == null
                        || !ref.isAfter(qr.getVentanaFin().plusSeconds(properties.getToleranciaRelojSegundos())));
        if (vigente) {
            return;
        }

        // 2. Gracia: versión inmediata anterior aceptada N segundos tras su ventanaFin.
        if (!Boolean.TRUE.equals(qr.getActivo()) && qr.getVentanaFin() != null && qr.getVersion() != null) {
            QrActividad actual = qrActividadRepository
                    .findTopByIdActividadOrderByVersionDesc(qr.getIdActividad())
                    .orElse(null);
            if (actual != null && actual.getVersion() != null
                    && actual.getVersion() == qr.getVersion() + 1
                    && !ref.isAfter(qr.getVentanaFin().plusSeconds(properties.getGraciaSegundos()))) {
                log.info("QR aceptado en gracia actividad={} version={}", qr.getIdActividad(), qr.getVersion());
                return;
            }
            throw new BusinessRuleException("QR expirado");
        }

        if (qr.getVersion() == null || !qr.getVersion().equals(parse.version())) {
            throw new BusinessRuleException("Versión de QR no coincide");
        }
        throw new BusinessRuleException("QR fuera de la ventana de tiempo");
    }

    private QrActividadResponse toResponse(QrActividad qr) {
        long expira = Duration.between(
                Instant.now(),
                qr.getVentanaFin().atZone(ZoneId.systemDefault()).toInstant()
        ).getSeconds();

        return new QrActividadResponse(
                qr.getIdQrActividad(),
                qr.getIdActividad(),
                qr.getVersion(),
                qr.getCodigoQr(),
                qr.getVentanaInicio(),
                qr.getVentanaFin(),
                (int) Math.max(0, expira),
                qr.getActivo()
        );
    }
}