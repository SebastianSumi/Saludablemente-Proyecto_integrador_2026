package upeu.edu.pe.Saludabemente.nutricional.jornada.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.EstadoPeriodo;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.response.PeriodoResponse;
import upeu.edu.pe.Saludabemente.catalogo.periodo.service.PeriodoService;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.nutricional.jornada.dto.request.JornadaEvaluacionRequest;
import upeu.edu.pe.Saludabemente.nutricional.jornada.dto.response.InscripcionJornadaResponse;
import upeu.edu.pe.Saludabemente.nutricional.jornada.dto.response.JornadaEvaluacionResponse;
import upeu.edu.pe.Saludabemente.nutricional.jornada.entity.InscripcionJornada;
import upeu.edu.pe.Saludabemente.nutricional.jornada.entity.JornadaEvaluacion;
import upeu.edu.pe.Saludabemente.nutricional.jornada.enums.EstadoInscripcion;
import upeu.edu.pe.Saludabemente.nutricional.jornada.enums.EstadoJornada;
import upeu.edu.pe.Saludabemente.nutricional.jornada.repository.InscripcionJornadaRepository;
import upeu.edu.pe.Saludabemente.nutricional.jornada.repository.JornadaEvaluacionRepository;
import upeu.edu.pe.Saludabemente.nutricional.jornada.service.JornadaEvaluacionService;
import upeu.edu.pe.Saludabemente.seguridad.api.SeguridadService;
import upeu.edu.pe.Saludabemente.seguridad.api.UsuarioPerfilDTO;
import upeu.edu.pe.Saludabemente.shared.entity.AuditableEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JornadaEvaluacionServiceImpl implements JornadaEvaluacionService {

    private static final String ROL_ADMINISTRADOR = "Administrador";
    private static final String ROL_ROOT = "ROOT";

    private final JornadaEvaluacionRepository jornadaRepository;
    private final InscripcionJornadaRepository inscripcionRepository;
    private final PeriodoService periodoService;
    private final SeguridadService seguridadService;

    // ===================================================================
    // Consultas
    // ===================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<JornadaEvaluacionResponse> buscar(
            EstadoJornada estado, Long idPeriodo, Pageable pageable) {
        return jornadaRepository.buscar(estado, idPeriodo, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public JornadaEvaluacionResponse obtener(Long idJornada) {
        return toResponse(buscarJornadaOFallar(idJornada));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InscripcionJornadaResponse> listarInscritos(Long idJornada) {
        validarAdministrador();
        JornadaEvaluacion jornada = buscarJornadaOFallar(idJornada);
        return inscripcionRepository.findByIdJornadaOrderByNumeroTurnoAsc(idJornada)
                .stream()
                .map(inscripcion -> toInscripcionResponse(inscripcion, jornada))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InscripcionJornadaResponse> misInscripciones() {
        UsuarioPerfilDTO perfil = seguridadService.obtenerMiPerfil();

        if (perfil.idPersona() == null) {
            return List.of();
        }

        List<InscripcionJornada> inscripciones =
                inscripcionRepository.findByIdPersonaOrderByFechaInscripcionDesc(
                        perfil.idPersona());

        if (inscripciones.isEmpty()) {
            return List.of();
        }

        Map<Long, JornadaEvaluacion> jornadas = jornadaRepository
                .findAllById(inscripciones.stream()
                        .map(InscripcionJornada::getIdJornada)
                        .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(
                        JornadaEvaluacion::getIdJornada, Function.identity()));

        return inscripciones.stream()
                .map(inscripcion -> toInscripcionResponse(
                        inscripcion, jornadas.get(inscripcion.getIdJornada())))
                .toList();
    }

    // ===================================================================
    // Administración
    // ===================================================================

    @Override
    @Transactional
    public JornadaEvaluacionResponse crear(JornadaEvaluacionRequest request) {
        UsuarioPerfilDTO perfil = validarAdministrador();
        obtenerPeriodoValido(request.getIdPeriodo());

        JornadaEvaluacion jornada = JornadaEvaluacion.builder()
                .idPeriodo(request.getIdPeriodo())
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion())
                .fecha(request.getFecha())
                .hora(request.getHora())
                .capacidad(request.getCapacidad())
                .estado(request.getEstado() != null ? request.getEstado() : EstadoJornada.ABIERTA)
                .build();

        aplicarUsuarioCreacion(jornada, perfil);

        return toResponse(jornadaRepository.save(jornada));
    }

    @Override
    @Transactional
    public JornadaEvaluacionResponse actualizar(
            Long idJornada, JornadaEvaluacionRequest request) {
        UsuarioPerfilDTO perfil = validarAdministrador();
        JornadaEvaluacion jornada = buscarJornadaOFallar(idJornada);

        obtenerPeriodoValido(request.getIdPeriodo());

        long inscritosActivos = contarInscritosActivos(idJornada);
        if (request.getCapacidad() < inscritosActivos) {
            throw new BusinessRuleException(
                    "La capacidad no puede ser menor a las inscripciones activas ("
                            + inscritosActivos + ")");
        }

        jornada.setIdPeriodo(request.getIdPeriodo());
        jornada.setNombre(request.getNombre().trim());
        jornada.setDescripcion(request.getDescripcion());
        jornada.setFecha(request.getFecha());
        jornada.setHora(request.getHora());
        jornada.setCapacidad(request.getCapacidad());
        if (request.getEstado() != null) {
            jornada.setEstado(request.getEstado());
        }

        aplicarUsuarioModificacion(jornada, perfil);

        return toResponse(jornadaRepository.save(jornada));
    }

    @Override
    @Transactional
    public JornadaEvaluacionResponse cambiarEstado(Long idJornada, EstadoJornada estado) {
        UsuarioPerfilDTO perfil = validarAdministrador();
        JornadaEvaluacion jornada = buscarJornadaOFallar(idJornada);

        if (jornada.getEstado() == estado) {
            throw new BusinessRuleException(
                    "La jornada ya está en estado " + estado);
        }

        jornada.setEstado(estado);
        aplicarUsuarioModificacion(jornada, perfil);

        return toResponse(jornadaRepository.save(jornada));
    }

    @Override
    @Transactional
    public InscripcionJornadaResponse cambiarEstadoInscripcion(
            Long idJornada, Long idInscripcion, EstadoInscripcion estado) {
        UsuarioPerfilDTO perfil = validarAdministrador();
        JornadaEvaluacion jornada = jornadaRepository.findByIdForUpdate(idJornada)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Jornada no encontrada: " + idJornada));

        InscripcionJornada inscripcion = inscripcionRepository.findById(idInscripcion)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inscripción no encontrada: " + idInscripcion));

        if (!jornada.getIdJornada().equals(inscripcion.getIdJornada())) {
            throw new BusinessRuleException(
                    "La inscripción no pertenece a la jornada indicada");
        }

        if (inscripcion.getEstado() == estado) {
            throw new BusinessRuleException(
                    "La inscripción ya está en estado " + estado);
        }

        if (estado == EstadoInscripcion.ACTIVA) {
            if (inscripcion.getEstado() != EstadoInscripcion.CANCELADA) {
                throw new BusinessRuleException(
                        "Solo una inscripción cancelada puede reactivarse");
            }
            validarCupoDisponible(jornada);
        } else if (estado == EstadoInscripcion.ASISTIO) {
            if (inscripcion.getEstado() != EstadoInscripcion.ACTIVA) {
                throw new BusinessRuleException(
                        "Solo una inscripción activa puede marcarse como asistida");
            }
        }

        inscripcion.setEstado(estado);
        aplicarUsuarioModificacion(inscripcion, perfil);

        return toInscripcionResponse(
                inscripcionRepository.save(inscripcion), jornada);
    }

    // ===================================================================
    // Autoservicio
    // ===================================================================

    @Override
    @Transactional
    public InscripcionJornadaResponse inscribirme(Long idJornada) {
        UsuarioPerfilDTO perfil = seguridadService.obtenerMiPerfil();

        if (!Boolean.TRUE.equals(perfil.activo())) {
            throw new BusinessRuleException(
                    "No se puede inscribir: la cuenta está inactiva");
        }

        if (perfil.idPersona() == null) {
            throw new BusinessRuleException(
                    "No se puede inscribir: la cuenta no tiene una persona asociada");
        }

        // Bloquea la jornada para serializar la asignación de turnos.
        JornadaEvaluacion jornada = jornadaRepository.findByIdForUpdate(idJornada)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Jornada no encontrada: " + idJornada));

        if (jornada.getEstado() != EstadoJornada.ABIERTA) {
            throw new BusinessRuleException(
                    "La jornada no está abierta para inscripciones. Estado actual: "
                            + jornada.getEstado());
        }

        InscripcionJornada existente = inscripcionRepository
                .findByIdJornadaAndIdPersona(idJornada, perfil.idPersona())
                .orElse(null);

        if (existente != null
                && existente.getEstado() != EstadoInscripcion.CANCELADA) {
            throw new BusinessRuleException(
                    "Ya tiene una inscripción activa en esta jornada. Turno: "
                            + existente.getNumeroTurno());
        }

        validarCupoDisponible(jornada);

        int siguienteTurno = inscripcionRepository.findMaxNumeroTurno(idJornada) + 1;

        InscripcionJornada inscripcion = (existente != null)
                ? existente
                : InscripcionJornada.builder()
                        .idJornada(idJornada)
                        .idPersona(perfil.idPersona())
                        .build();

        inscripcion.setDocumentoIdentidad(perfil.documentoIdentidad());
        inscripcion.setNombresCompletos(perfil.nombreCompleto());
        inscripcion.setNumeroTurno(siguienteTurno);
        inscripcion.setFechaInscripcion(LocalDateTime.now());
        inscripcion.setEstado(EstadoInscripcion.ACTIVA);
        if (existente == null) {
            aplicarUsuarioCreacion(inscripcion, perfil);
        }
        aplicarUsuarioModificacion(inscripcion, perfil);

        return toInscripcionResponse(
                inscripcionRepository.save(inscripcion), jornada);
    }

    // ===================================================================
    // Helpers
    // ===================================================================

    private JornadaEvaluacion buscarJornadaOFallar(Long idJornada) {
        return jornadaRepository.findById(idJornada)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Jornada de evaluación no encontrada: " + idJornada));
    }

    private PeriodoResponse obtenerPeriodoValido(Long idPeriodo) {
        PeriodoResponse periodo = periodoService.obtener(idPeriodo);
        if (periodo.getEstado() == EstadoPeriodo.CERRADO) {
            throw new BusinessRuleException(
                    "No se puede usar un periodo CERRADO: " + periodo.getCodigo());
        }
        return periodo;
    }

    private long contarInscritosActivos(Long idJornada) {
        return inscripcionRepository.countByIdJornadaAndEstadoIn(
                idJornada, List.of(EstadoInscripcion.ACTIVA, EstadoInscripcion.ASISTIO));
    }

    private void validarCupoDisponible(JornadaEvaluacion jornada) {
        long inscritos = contarInscritosActivos(jornada.getIdJornada());
        if (jornada.getCapacidad() != null && inscritos >= jornada.getCapacidad()) {
            throw new BusinessRuleException(
                    "La jornada alcanzó su capacidad máxima ("
                            + jornada.getCapacidad() + ")");
        }
    }

    private UsuarioPerfilDTO validarAdministrador() {
        UsuarioPerfilDTO perfil = seguridadService.obtenerMiPerfil();

        boolean esAdmin = perfil.roles() != null && perfil.roles().stream()
                .anyMatch(rol -> ROL_ADMINISTRADOR.equalsIgnoreCase(rol)
                        || ROL_ROOT.equalsIgnoreCase(rol));

        if (!esAdmin) {
            throw new BusinessRuleException(
                    "No tiene permisos de administrador para esta operación");
        }
        return perfil;
    }

    private JornadaEvaluacionResponse toResponse(JornadaEvaluacion jornada) {
        long inscritos = contarInscritosActivos(jornada.getIdJornada());
        Integer capacidad = jornada.getCapacidad();
        long cuposDisponibles = capacidad == null
                ? 0
                : Math.max(0, capacidad - inscritos);

        return JornadaEvaluacionResponse.builder()
                .idJornada(jornada.getIdJornada())
                .nombre(jornada.getNombre())
                .descripcion(jornada.getDescripcion())
                .fecha(jornada.getFecha())
                .hora(jornada.getHora())
                .capacidad(capacidad)
                .idPeriodo(jornada.getIdPeriodo())
                .periodoCodigo(obtenerCodigoPeriodo(jornada.getIdPeriodo()))
                .estado(jornada.getEstado())
                .inscritos(inscritos)
                .cuposDisponibles(cuposDisponibles)
                .fechaCreacion(jornada.getFechaCreacion())
                .usuarioCreacion(jornada.getUsuarioCreacion())
                .build();
    }

    private InscripcionJornadaResponse toInscripcionResponse(
            InscripcionJornada inscripcion, JornadaEvaluacion jornada) {
        return InscripcionJornadaResponse.builder()
                .idInscripcion(inscripcion.getIdInscripcion())
                .idJornada(inscripcion.getIdJornada())
                .nombreJornada(jornada != null ? jornada.getNombre() : null)
                .fechaJornada(jornada != null ? jornada.getFecha() : null)
                .horaJornada(jornada != null ? jornada.getHora() : null)
                .idPersona(inscripcion.getIdPersona())
                .documentoIdentidad(inscripcion.getDocumentoIdentidad())
                .nombresCompletos(inscripcion.getNombresCompletos())
                .numeroTurno(inscripcion.getNumeroTurno())
                .fechaInscripcion(inscripcion.getFechaInscripcion())
                .estado(inscripcion.getEstado())
                .build();
    }

    private String obtenerCodigoPeriodo(Long idPeriodo) {
        if (idPeriodo == null) {
            return null;
        }
        try {
            return periodoService.obtener(idPeriodo).getCodigo();
        } catch (ResourceNotFoundException ex) {
            return null;
        }
    }

    private void aplicarUsuarioCreacion(AuditableEntity entity, UsuarioPerfilDTO perfil) {
        entity.setUsuarioCreacion(recortarUsuario(perfil.email()));
    }

    private void aplicarUsuarioModificacion(AuditableEntity entity, UsuarioPerfilDTO perfil) {
        entity.setUsuarioModificacion(recortarUsuario(perfil.email()));
    }

    private String recortarUsuario(String valor) {
        if (valor == null || valor.isBlank()) {
            return "SYSTEM";
        }
        return valor.length() > 30 ? valor.substring(0, 30) : valor;
    }
}
