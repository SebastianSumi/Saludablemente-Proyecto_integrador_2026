package upeu.edu.pe.Saludabemente.catalogo.periodo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.EstadoPeriodo;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.request.PeriodoRequest;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.response.PeriodoResponse;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.response.PeriodoResumen;
import upeu.edu.pe.Saludabemente.catalogo.periodo.entity.Periodo;
import upeu.edu.pe.Saludabemente.catalogo.periodo.mapper.PeriodoMapper;
import upeu.edu.pe.Saludabemente.catalogo.periodo.repository.PeriodoRepository;
import upeu.edu.pe.Saludabemente.catalogo.periodo.service.PeriodoService;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PeriodoServiceImpl implements PeriodoService {

    /**
     * Usuario que firma el cierre de un periodo. Es una marca de NEGOCIO
     * (quien cerro la campana), no de auditoria tecnica. La auditoria
     * (fechaCreacion, usuarioCreacion, fechaModificacion, usuarioModificacion)
     * la gestiona {@code AuditableEntity} automaticamente en @PrePersist/@PreUpdate.
     *
     * <p>Cuando exista el modulo Seguridad (U2), se reemplaza por el
     * usuario autenticado del contexto.</p>
     */
    private static final String USUARIO_CIERRE = "system";

    private final PeriodoRepository periodoRepository;
    private final PeriodoMapper periodoMapper;

    // -------------------------------------------------------------------
    // Consultas internas (uso del controller)
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<PeriodoResponse> buscar(EstadoPeriodo estado, Sort sort) {
        List<Periodo> periodos = (estado == null)
                ? periodoRepository.findAll(sort)
                : periodoRepository.findByEstado(estado, sort);
        return periodos.stream().map(periodoMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PeriodoResponse obtener(Long id) {
        return periodoMapper.toResponse(buscarOFallar(id));
    }

    // -------------------------------------------------------------------
    // Servicios publicos para otros modulos
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public PeriodoResponse obtenerVigente() {
        return periodoRepository.findFirstByEstado(EstadoPeriodo.VIGENTE)
                .map(periodoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No hay ningun periodo en estado VIGENTE"));
    }

    @Override
    @Transactional(readOnly = true)
    public PeriodoResponse obtenerPorCodigo(String codigo) {
        return periodoRepository.findByCodigo(codigo)
                .map(periodoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Periodo no encontrado con codigo: " + codigo));
    }

    @Override
    @Transactional(readOnly = true)
    public Periodo buscarOFallar(Long id) {
        return periodoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Periodo no encontrado: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PeriodoResumen> listarResumen() {
        Sort sort = Sort.by(Sort.Direction.DESC, "codigo");
        return periodoRepository.findAll(sort).stream()
                .map(periodoMapper::toResumen)
                .toList();
    }

    // -------------------------------------------------------------------
    // Comandos
    // -------------------------------------------------------------------

    @Override
    @Transactional
    public PeriodoResponse crear(PeriodoRequest request) {
        validarCodigoUnico(request.codigo());
        validarRangoFechas(request);

        Periodo periodo = periodoMapper.toEntity(request);
        periodo.setEstado(EstadoPeriodo.PLANIFICADO);
        // AuditableEntity rellena fechaCreacion y usuarioCreacion en @PrePersist.

        return periodoMapper.toResponse(periodoRepository.save(periodo));
    }

    @Override
    @Transactional
    public PeriodoResponse actualizar(Long id, PeriodoRequest request) {
        Periodo periodo = buscarOFallar(id);

        if (!periodo.getCodigo().equals(request.codigo())) {
            validarCodigoUnico(request.codigo());
        }
        validarRangoFechas(request);

        periodo.setCodigo(request.codigo());
        periodo.setDescripcion(request.descripcion());
        periodo.setFechaInicio(request.fechaInicio());
        periodo.setFechaFin(request.fechaFin());
        // AuditableEntity rellena fechaModificacion y usuarioModificacion en @PreUpdate.

        return periodoMapper.toResponse(periodoRepository.save(periodo));
    }

    @Override
    @Transactional
    public PeriodoResponse marcarVigente(Long id) {
        Periodo periodo = buscarOFallar(id);

        if (periodo.getEstado() == EstadoPeriodo.VIGENTE) {
            throw new BusinessRuleException(
                    "El periodo ya esta en estado VIGENTE: " + periodo.getCodigo());
        }
        if (periodo.getEstado() == EstadoPeriodo.CERRADO) {
            throw new BusinessRuleException(
                    "No se puede marcar como VIGENTE un periodo CERRADO: " + periodo.getCodigo());
        }

        // Validacion en memoria. El indice funcional UQ_PERIODO_VIGENTE en Oracle
        // es la segunda linea de defensa ante condiciones de carrera.
        periodoRepository.findFirstByEstado(EstadoPeriodo.VIGENTE).ifPresent(vigenteActual -> {
            throw new BusinessRuleException(
                    "Ya existe un periodo VIGENTE: " + vigenteActual.getCodigo()
                            + ". Cierrelo antes de marcar otro como VIGENTE.");
        });

        periodo.setEstado(EstadoPeriodo.VIGENTE);
        // AuditableEntity rellena fechaModificacion y usuarioModificacion en @PreUpdate.

        return periodoMapper.toResponse(periodoRepository.save(periodo));
    }

    @Override
    @Transactional
    public PeriodoResponse cerrar(Long id) {
        Periodo periodo = buscarOFallar(id);

        if (periodo.getEstado() == EstadoPeriodo.CERRADO) {
            throw new BusinessRuleException(
                    "El periodo ya esta CERRADO: " + periodo.getCodigo());
        }

        periodo.setEstado(EstadoPeriodo.CERRADO);
        periodo.setCerradoEn(LocalDateTime.now());
        periodo.setCerradoPor(USUARIO_CIERRE);
        // AuditableEntity rellena fechaModificacion y usuarioModificacion en @PreUpdate.

        return periodoMapper.toResponse(periodoRepository.save(periodo));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Periodo periodo = buscarOFallar(id);

        if (periodo.getEstado() == EstadoPeriodo.VIGENTE) {
            throw new BusinessRuleException(
                    "No se puede eliminar un periodo VIGENTE. Cierrelo primero.");
        }

        // Baja logica: pasa a CERRADO.
        periodo.setEstado(EstadoPeriodo.CERRADO);
        periodo.setCerradoEn(LocalDateTime.now());
        periodo.setCerradoPor(USUARIO_CIERRE);
        // AuditableEntity rellena fechaModificacion y usuarioModificacion en @PreUpdate.
        periodoRepository.save(periodo);
    }

    // -------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------

    private void validarCodigoUnico(String codigo) {
        if (periodoRepository.existsByCodigo(codigo)) {
            throw new BusinessRuleException(
                    "Ya existe un periodo con el codigo: " + codigo);
        }
    }

    private void validarRangoFechas(PeriodoRequest request) {
        if (!request.fechaInicio().isBefore(request.fechaFin())) {
            throw new BusinessRuleException(
                    "La fecha de inicio debe ser anterior a la fecha de fin");
        }
    }
}