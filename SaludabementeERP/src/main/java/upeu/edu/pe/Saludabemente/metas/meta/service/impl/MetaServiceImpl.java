package upeu.edu.pe.Saludabemente.metas.meta.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.metas.meta.dto.*;
import upeu.edu.pe.Saludabemente.metas.meta.entity.MetaEntity;
import upeu.edu.pe.Saludabemente.metas.meta.mapper.MetaMapper;
import upeu.edu.pe.Saludabemente.metas.meta.repository.CheckInRepository;
import upeu.edu.pe.Saludabemente.metas.meta.repository.MetaRepository;
import upeu.edu.pe.Saludabemente.metas.meta.service.GamificacionService;
import upeu.edu.pe.Saludabemente.metas.meta.service.MetaService;
import upeu.edu.pe.Saludabemente.metas.shared.enums.EstadoMeta;
import upeu.edu.pe.Saludabemente.metas.shared.enums.ModalidadMeta;
import upeu.edu.pe.Saludabemente.metas.shared.enums.TipoCalculo;
import upeu.edu.pe.Saludabemente.metas.shared.enums.TipoOrigen;
import upeu.edu.pe.Saludabemente.nutricional.api.ContextoClinicoDTO;
import upeu.edu.pe.Saludabemente.nutricional.api.EvaluacionMetasApiService;
import upeu.edu.pe.Saludabemente.personal.api.PersonalNotificacionesApiService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MetaServiceImpl implements MetaService {

    private final MetaRepository metaRepository;
    private final CheckInRepository checkInRepository;
    private final MetaMapper metaMapper;
    private final GamificacionService gamificacionService;
    private final PersonalNotificacionesApiService personaApi;
    private final EvaluacionMetasApiService evaluacionApi;

    // =================================================================
    // CREAR META PERSONAL
    // =================================================================
    @Override
    @Transactional
    public MetaResponseDTO crearPersonal(MetaRequestDTO dto, String usuario) {

        validarCoherencia(dto.getModalidad(), dto.getTipoCalculo(),
                dto.getValorInicial(), dto.getValorObjetivo());

        if (!personaApi.existeYEstaActiva(dto.getIdPersona())) {
            throw new ResourceNotFoundException(
                    "Persona no encontrada o inactiva: " + dto.getIdPersona());
        }

        MetaEntity entity = metaMapper.toEntityFromPersonal(dto);
        entity.setTipoOrigen(TipoOrigen.PERSONAL);
        entity.setEstado(EstadoMeta.ACTIVA);
        entity.setValorActual(dto.getTipoCalculo() == TipoCalculo.ACUMULADO
                ? BigDecimal.ZERO
                : dto.getValorInicial());
        entity.setCreadoPor(usuario);

        MetaEntity guardada = metaRepository.save(entity);
        log.info("Meta personal creada id={} persona={} por={}",
                guardada.getId(), guardada.getIdPersona(), usuario);
        return metaMapper.toResponse(guardada);
    }

    // =================================================================
    // CREAR META CLINICA
    // =================================================================
    @Override
    @Transactional
    public MetaResponseDTO crearClinica(MetaClinicaRequestDTO dto, String usuario) {

        validarCoherencia(dto.getModalidad(), dto.getTipoCalculo(),
                dto.getValorInicial(), dto.getValorObjetivo());

        if (!personaApi.existeYEstaActiva(dto.getIdPersona())) {
            throw new ResourceNotFoundException(
                    "Persona no encontrada o inactiva: " + dto.getIdPersona());
        }

        if (!evaluacionApi.existeEvaluacion(dto.getIdEvaluacionOrigen(), dto.getIdPersona())) {
            throw new ResourceNotFoundException(
                    "Evaluacion no encontrada o no pertenece al colaborador: "
                            + dto.getIdEvaluacionOrigen());
        }

        ContextoClinicoDTO contexto = evaluacionApi
                .obtenerContextoClinico(dto.getIdEvaluacionOrigen());

        BigDecimal valorInicial = contexto.valores().get(dto.getIndicador());
        if (valorInicial == null) {
            throw new BusinessRuleException(
                    "El indicador '" + dto.getIndicador()
                            + "' no fue medido en la evaluacion origen");
        }

        MetaEntity entity = metaMapper.toEntityFromClinica(dto);
        entity.setTipoOrigen(TipoOrigen.CLINICA);
        entity.setEstado(EstadoMeta.ACTIVA);
        entity.setValorInicial(valorInicial);
        entity.setValorActual(dto.getTipoCalculo() == TipoCalculo.ACUMULADO
                ? BigDecimal.ZERO
                : valorInicial);
        entity.setCreadoPor(usuario);

        MetaEntity guardada = metaRepository.save(entity);
        log.info("Meta clinica creada id={} persona={} evaluacion={} alerta={} por={}",
                guardada.getId(), guardada.getIdPersona(),
                guardada.getIdEvaluacionOrigen(), guardada.getIdAlertaOrigen(), usuario);
        return metaMapper.toResponse(guardada);
    }

    // =================================================================
    // CONSULTAS
    // =================================================================
    @Override
    public MetaResponseDTO obtener(Long idMeta) {
        return metaMapper.toResponse(buscarOFallar(idMeta));
    }

    @Override
    public List<MetaResponseDTO> listarPorPersona(Long idPersona, EstadoMeta estado) {
        List<MetaEntity> metas = (estado == null)
                ? metaRepository.findByIdPersona(idPersona)
                : metaRepository.findByIdPersonaAndEstado(idPersona, estado);
        return metas.stream().map(metaMapper::toResponse).toList();
    }

    @Override
    public ProgresoMetaResponseDTO obtenerProgreso(Long idMeta) {
        MetaEntity m = buscarOFallar(idMeta);
        BigDecimal avance = calcularAvance(m);
        int racha = gamificacionService.calcularRacha(idMeta);
        long total = checkInRepository.countByMetaId(idMeta);
        LocalDate ultimo = checkInRepository.findUltimaFecha(idMeta);

        return new ProgresoMetaResponseDTO(
                m.getId(), m.getIdPersona(), m.getDescripcion(),
                m.getPilar(), m.getModalidad(), m.getTipoOrigen(), m.getEstado(),
                m.getIndicador(), m.getUnidad(),
                m.getValorInicial(), m.getValorObjetivo(), m.getValorActual(),
                avance, racha, total,
                m.getFechaInicio(), m.getFechaObjetivo(), ultimo,
                m.getEstado() == EstadoMeta.DESVIADA);
    }

    // =================================================================
    // CAMBIO DE ESTADO
    // =================================================================
    @Override
    @Transactional
    public void cambiarEstado(Long idMeta, EstadoMeta nuevo, String usuario) {
        MetaEntity m = buscarOFallar(idMeta);
        if (m.getEstado() == nuevo) return;

        EstadoMeta anterior = m.getEstado();
        m.setEstado(nuevo);
        m.setModificadoPor(usuario);
        if (nuevo == EstadoMeta.COMPLETADA || nuevo == EstadoMeta.ABANDONADA) {
            m.setFechaCierre(LocalDateTime.now());
        }
        metaRepository.save(m);
        log.info("Meta {} cambio estado {} -> {} por {}",
                idMeta, anterior, nuevo, usuario);
    }

    // =================================================================
    // HELPERS PUBLICOS AL MODULO
    // =================================================================

    /** Calculo unico de % avance, reutilizado por CheckInServiceImpl. */
    public static BigDecimal calcularAvance(MetaEntity m) {
        BigDecimal inicial = m.getValorInicial() == null ? BigDecimal.ZERO : m.getValorInicial();
        BigDecimal objetivo = m.getValorObjetivo();
        BigDecimal actual  = m.getValorActual() == null ? BigDecimal.ZERO : m.getValorActual();

        if (objetivo.compareTo(inicial) == 0) {
            return actual.compareTo(objetivo) >= 0
                    ? BigDecimal.valueOf(100)
                    : BigDecimal.ZERO;
        }
        BigDecimal avance = actual.subtract(inicial)
                .divide(objetivo.subtract(inicial), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        if (avance.signum() < 0) avance = BigDecimal.ZERO;
        if (avance.compareTo(BigDecimal.valueOf(100)) > 0) {
            avance = BigDecimal.valueOf(100);
        }
        return avance.setScale(2, RoundingMode.HALF_UP);
    }

    // =================================================================
    // HELPERS PRIVADOS
    // =================================================================
    private MetaEntity buscarOFallar(Long id) {
        return metaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meta no encontrada: " + id));
    }

    private void validarCoherencia(ModalidadMeta modalidad, TipoCalculo calculo,
                                   BigDecimal inicial, BigDecimal objetivo) {
        if (modalidad == null || calculo == null || inicial == null || objetivo == null) {
            throw new BusinessRuleException(
                    "Modalidad, tipo de calculo y valores son obligatorios");
        }
        if (objetivo.signum() <= 0) {
            throw new BusinessRuleException("El valor objetivo debe ser positivo");
        }
        if (calculo == TipoCalculo.ACUMULADO && inicial.signum() != 0) {
            throw new BusinessRuleException(
                    "En metas ACUMULADO el valor inicial debe ser 0");
        }
        if (modalidad == ModalidadMeta.CUALITATIVA
                && calculo != TipoCalculo.ACUMULADO) {
            throw new BusinessRuleException(
                    "Las metas cualitativas (habitos) deben usar tipo de calculo ACUMULADO");
        }
    }

    // =================================================================
// CREAR META DESDE SUGERENCIA IA (endpoint /api/v1/metas/sugeridas)
// =================================================================
    @Override
    @Transactional
    public MetaResponseDTO crearDesdeSugerencia(MetaSugeridaRequestDTO dto, String usuario) {

        if (!dto.getFechaLimite().isAfter(LocalDate.now())) {
            throw new BusinessRuleException(
                    "La fecha limite debe ser posterior a hoy");
        }
        if (!personaApi.existeYEstaActiva(dto.getIdPersona())) {
            throw new ResourceNotFoundException(
                    "Persona no encontrada o inactiva: " + dto.getIdPersona());
        }

        MetaEntity entity = new MetaEntity();
        entity.setIdPersona(dto.getIdPersona());
        entity.setPilar(dto.getPilar());
        entity.setModalidad(ModalidadMeta.CUANTITATIVA);
        entity.setTipoOrigen(TipoOrigen.PERSONAL);
        entity.setTipoCalculo(TipoCalculo.ULTIMO_VALOR);
        entity.setIndicador(dto.getTipoMeta());
        entity.setUnidad(null);
        entity.setDescripcion(dto.getDescripcion());
        entity.setValorInicial(dto.getValorInicial());
        entity.setValorObjetivo(dto.getValorObjetivo());
        entity.setValorActual(dto.getValorInicial());
        entity.setFechaInicio(LocalDate.now());
        entity.setFechaObjetivo(dto.getFechaLimite());
        entity.setEstado(EstadoMeta.ACTIVA);
        entity.setCreadoPor(usuario);

        MetaEntity guardada = metaRepository.save(entity);
        log.info("Meta sugerida creada id={} persona={} indicador={} por={}",
                guardada.getId(), guardada.getIdPersona(),
                guardada.getIndicador(), usuario);
        return metaMapper.toResponse(guardada);
    }
}