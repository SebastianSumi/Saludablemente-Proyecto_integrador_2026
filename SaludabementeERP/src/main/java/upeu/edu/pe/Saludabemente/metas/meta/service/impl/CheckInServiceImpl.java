package upeu.edu.pe.Saludabemente.metas.meta.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.metas.meta.dto.CheckInRequestDTO;
import upeu.edu.pe.Saludabemente.metas.meta.dto.ProgresoMetaResponseDTO;
import upeu.edu.pe.Saludabemente.metas.meta.entity.CheckInEntity;
import upeu.edu.pe.Saludabemente.metas.meta.entity.MetaEntity;
import upeu.edu.pe.Saludabemente.metas.meta.mapper.CheckInMapper;
import upeu.edu.pe.Saludabemente.metas.meta.repository.CheckInRepository;
import upeu.edu.pe.Saludabemente.metas.meta.repository.MetaRepository;
import upeu.edu.pe.Saludabemente.metas.meta.service.CheckInService;
import upeu.edu.pe.Saludabemente.metas.meta.service.MetaService;
import upeu.edu.pe.Saludabemente.metas.shared.enums.EstadoMeta;
import upeu.edu.pe.Saludabemente.metas.shared.enums.TipoCalculo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInServiceImpl implements CheckInService {

    private final CheckInRepository checkInRepository;
    private final MetaRepository metaRepository;
    private final CheckInMapper checkInMapper;
    private final MetaService metaService;

    @Override
    @Transactional
    public ProgresoMetaResponseDTO registrar(CheckInRequestDTO dto, String usuario) {

        MetaEntity meta = metaRepository.findById(dto.getIdMeta())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Meta no encontrada: " + dto.getIdMeta()));

        // 1. Pertenencia
        if (!meta.getIdPersona().equals(dto.getIdPersona())) {
            throw new BusinessRuleException(
                    "La meta no pertenece al colaborador indicado");
        }

        // 2. Estado
        if (meta.getEstado() != EstadoMeta.ACTIVA) {
            throw new BusinessRuleException(
                    "La meta no esta ACTIVA (estado actual: " + meta.getEstado() + ")");
        }

        // 3. Fecha en rango
        LocalDate fecha = (dto.getFecha() == null) ? LocalDate.now() : dto.getFecha();
        if (fecha.isBefore(meta.getFechaInicio())) {
            throw new BusinessRuleException(
                    "La fecha del check-in es anterior al inicio de la meta");
        }
        if (fecha.isAfter(meta.getFechaObjetivo())) {
            throw new BusinessRuleException(
                    "La meta ya vencio el " + meta.getFechaObjetivo());
        }

        // 4. Anti-duplicado diario
        if (checkInRepository.existsByMetaIdAndFecha(meta.getId(), fecha)) {
            throw new BusinessRuleException(
                    "Ya existe un check-in para la meta " + meta.getId()
                            + " en la fecha " + fecha);
        }

        // 5. Coherencia del valor segun modalidad
        BigDecimal valor = resolverValor(meta, dto);

        // 6. Persistir detalle
        CheckInEntity checkIn = checkInMapper.toEntity(dto);
        checkIn.setMeta(meta);
        checkIn.setFecha(fecha);
        checkIn.setValorReportado(valor);
        CheckInEntity guardado = checkInRepository.save(checkIn);

        // 7. Actualizar cabecera
        actualizarCabecera(meta, valor);

        // 8. Transicion de estado
        BigDecimal avance = MetaServiceImpl.calcularAvance(meta);
        if (avance.compareTo(BigDecimal.valueOf(100)) >= 0
                && meta.getEstado() != EstadoMeta.COMPLETADA) {
            meta.setEstado(EstadoMeta.COMPLETADA);
            meta.setFechaCierre(LocalDateTime.now());
            log.info("Meta {} COMPLETADA por {}", meta.getId(), usuario);
        } else if (meta.getEstado() == EstadoMeta.DESVIADA) {
            meta.setEstado(EstadoMeta.ACTIVA);
            log.info("Meta {} reactivada por {}", meta.getId(), usuario);
        }

        meta.setModificadoPor(usuario);
        metaRepository.save(meta);

        log.info("Check-in id={} meta={} valor={} por={}",
                guardado.getId(), meta.getId(), valor, usuario);

        return metaService.obtenerProgreso(meta.getId());
    }

    @Override
    public java.util.List<ProgresoMetaResponseDTO> historial(Long idMeta) {
        // Confirmar existencia de la meta
        if (!metaRepository.existsById(idMeta)) {
            throw new ResourceNotFoundException("Meta no encontrada: " + idMeta);
        }
        return checkInRepository.findByMetaIdOrderByFechaDesc(idMeta).stream()
                .map(c -> metaService.obtenerProgreso(c.getMeta().getId()))
                .distinct()
                .toList();
    }

    // =================================================================
    // HELPERS
    // =================================================================

    private BigDecimal resolverValor(MetaEntity meta, CheckInRequestDTO dto) {
        if (meta.getModalidad() == upeu.edu.pe.Saludabemente.metas.shared.enums.ModalidadMeta.CUALITATIVA) {
            if (dto.getLogrado() == null) {
                throw new BusinessRuleException(
                        "Las metas cualitativas requieren el campo 'logrado' (true/false)");
            }
            return dto.getLogrado() ? BigDecimal.ONE : BigDecimal.ZERO;
        }
        if (dto.getValorReportado() == null || dto.getValorReportado().signum() < 0) {
            throw new BusinessRuleException(
                    "Las metas cuantitativas requieren un 'valorReportado' >= 0");
        }
        return dto.getValorReportado();
    }

    private void actualizarCabecera(MetaEntity meta, BigDecimal valor) {
        if (meta.getTipoCalculo() == TipoCalculo.ACUMULADO) {
            BigDecimal base = (meta.getValorActual() == null)
                    ? BigDecimal.ZERO
                    : meta.getValorActual();
            meta.setValorActual(base.add(valor));
        } else {
            meta.setValorActual(valor);
        }
    }
}