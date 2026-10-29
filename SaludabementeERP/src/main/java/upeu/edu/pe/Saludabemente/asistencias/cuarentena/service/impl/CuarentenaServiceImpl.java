package upeu.edu.pe.Saludabemente.asistencias.cuarentena.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.asistencias.cuarentena.dto.request.ResolverCuarentenaRequest;
import upeu.edu.pe.Saludabemente.asistencias.cuarentena.dto.response.CuarentenaResponse;
import upeu.edu.pe.Saludabemente.asistencias.cuarentena.entity.AsistenciaCuarentena;
import upeu.edu.pe.Saludabemente.asistencias.cuarentena.repository.CuarentenaRepository;
import upeu.edu.pe.Saludabemente.asistencias.cuarentena.service.CuarentenaService;
import upeu.edu.pe.Saludabemente.asistencias.enums.EscaneadoPor;
import upeu.edu.pe.Saludabemente.asistencias.enums.EstadoAsistencia;
import upeu.edu.pe.Saludabemente.asistencias.enums.EstadoCuarentena;
import upeu.edu.pe.Saludabemente.asistencias.enums.MetodoRegistro;
import upeu.edu.pe.Saludabemente.asistencias.enums.MotivoCuarentena;
import upeu.edu.pe.Saludabemente.asistencias.enums.SyncStatus;
import upeu.edu.pe.Saludabemente.asistencias.registro.entity.Asistencia;
import upeu.edu.pe.Saludabemente.asistencias.registro.repository.AsistenciaRepository;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CuarentenaServiceImpl implements CuarentenaService {

    private final CuarentenaRepository cuarentenaRepository;
    private final AsistenciaRepository asistenciaRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<CuarentenaResponse> listarBandeja(
            EstadoCuarentena estado,
            MotivoCuarentena motivo,
            Long idActividad,
            Long idPersona,
            LocalDateTime desde,
            LocalDateTime hasta,
            Pageable pageable) {

        return cuarentenaRepository
                .buscarConFiltros(estado, motivo, idActividad, idPersona, desde, hasta, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CuarentenaResponse obtener(Long idCuarentena) {
        AsistenciaCuarentena c = cuarentenaRepository.findById(idCuarentena)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cuarentena no encontrada: " + idCuarentena));
        return toResponse(c);
    }

    @Override
    @Transactional
    public CuarentenaResponse resolver(Long idCuarentena, ResolverCuarentenaRequest request) {

        AsistenciaCuarentena c = cuarentenaRepository.findById(idCuarentena)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cuarentena no encontrada: " + idCuarentena));

        if (c.getEstado() != EstadoCuarentena.PENDIENTE) {
            throw new BusinessRuleException(
                    "Este registro ya fue resuelto: " + c.getEstado());
        }

        String decision = request.decision().toUpperCase();
        if ("APROBAR".equals(decision)) {
            migrarAAsistencia(c, request);
            c.setEstado(EstadoCuarentena.APROBADO);
        } else if ("RECHAZAR".equals(decision)) {
            c.setEstado(EstadoCuarentena.RECHAZADO);
        } else {
            throw new BusinessRuleException("Decisión inválida: " + decision);
        }

        c.setObservaciones(request.observaciones());
        c.setIdUsuarioResuelve(request.idUsuarioResuelve());
        c.setFechaResolucion(LocalDateTime.now());

        AsistenciaCuarentena guardada = cuarentenaRepository.save(c);
        log.info("Cuarentena resuelta id={} decision={} usuario={}",
                idCuarentena, decision, request.idUsuarioResuelve());

        return toResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPendientes() {
        return cuarentenaRepository.countByEstado(EstadoCuarentena.PENDIENTE);
    }

    // ============================================================
    // MIGRACIÓN
    // ============================================================
    private void migrarAAsistencia(AsistenciaCuarentena c, ResolverCuarentenaRequest request) {

        // Si ya existe la asistencia (por idLocal), no duplicar
        if (asistenciaRepository.existsByIdLocal(c.getIdLocal())) {
            log.warn("Cuarentena {} ya tiene asistencia con idLocal={}", c.getIdCuarentena(), c.getIdLocal());
            return;
        }

        Asistencia asistencia = Asistencia.builder()
                .idLocal(c.getIdLocal())
                .idActividad(c.getIdActividad())
                .idPersona(c.getIdPersona())
                .tipoAsistencia(c.getTipoAsistencia())
                .metodoRegistro(MetodoRegistro.MANUAL) // aprobación administrativa
                .escaneadoPor(EscaneadoPor.COORDINADOR)
                .estado(EstadoAsistencia.ASISTIO)
                .horaMarcado(c.getHoraMarcado())
                .latitud(c.getLatitud())
                .longitud(c.getLongitud())
                .distanciaGeocercaM(c.getDistanciaGeocercaM())
                .flagSospechoso(false) // aprobado por humano
                .syncStatus(SyncStatus.SYNCED)
                .retryCount(0)
                .lastSyncAttempt(LocalDateTime.now())
                .idUsuarioRegistro(request.idUsuarioResuelve())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        asistenciaRepository.save(asistencia);
        log.info("Cuarentena migrada a ASISTENCIA idLocal={}", c.getIdLocal());
    }

    private CuarentenaResponse toResponse(AsistenciaCuarentena c) {
        return new CuarentenaResponse(
                c.getIdCuarentena(),
                c.getIdLocal(),
                c.getIdActividad(),
                c.getIdPersona(),
                c.getTipoAsistencia(),
                c.getHoraMarcado(),
                c.getLatitud(),
                c.getLongitud(),
                c.getDistanciaGeocercaM(),
                c.getMotivo(),
                c.getEstado(),
                c.getObservaciones(),
                c.getIdUsuarioResuelve(),
                c.getFechaCreacion(),
                c.getFechaResolucion()
        );
    }
}