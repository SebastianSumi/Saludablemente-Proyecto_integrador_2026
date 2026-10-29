package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.EtiquetaClinicaRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.EtiquetaClinicaResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.entity.EtiquetaClinica;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.entity.EtiquetaParametro;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.mapper.EtiquetaClinicaMapper;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.repository.AlertaEtiquetaRepository;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.repository.EtiquetaClinicaRepository;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.parametro.entity.ParametroEvaluacion;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.parametro.repository.ParametroEvaluacionRepository;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EtiquetaClinicaServiceImpl implements EtiquetaClinicaService {

    private static final String ESTADO_ACTIVO = "ACTIVO";
    private static final String ESTADO_INACTIVO = "INACTIVO";

    private final EtiquetaClinicaRepository repository;
    private final ParametroEvaluacionRepository parametroRepository;
    private final AlertaEtiquetaRepository alertaEtiquetaRepository;
    private final EtiquetaClinicaMapper mapper;
    private final BloqueoAsociaciones bloqueo;

    @Override
    @Transactional(readOnly = true)
    public List<EtiquetaClinicaResponseDto> listar(String estado) {
        if (estado != null && !estado.isBlank() && !Set.of(ESTADO_ACTIVO, ESTADO_INACTIVO).contains(estado))
            throw new IllegalArgumentException("El estado debe ser ACTIVO o INACTIVO");
        List<EtiquetaClinica> etiquetas = (estado != null && !estado.isBlank())
                ? repository.findByEstado(estado)
                : repository.findAll();
        return etiquetas.stream()
                .map(e -> mapper.toResponseDto(
                        e,
                        alertaEtiquetaRepository.findAlertasByEtiqueta(e.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EtiquetaClinicaResponseDto obtener(Long id) {
        EtiquetaClinica entidad = buscarOFallar(id);
        return mapper.toResponseDto(
                entidad,
                alertaEtiquetaRepository.findAlertasByEtiqueta(id));
    }

    @Override
    @Transactional
    public EtiquetaClinicaResponseDto crear(EtiquetaClinicaRequestDto dto) {
        bloqueo.adquirir();
        if (repository.existsByCodigo(dto.codigo())) {
            throw new BusinessRuleException(
                    "Ya existe una etiqueta con el codigo: " + dto.codigo());
        }

        EtiquetaClinica entidad = new EtiquetaClinica();
        entidad.setCodigo(dto.codigo());
        entidad.setNombre(dto.nombre());
        entidad.setDescripcion(dto.descripcion());
        entidad.setColorHex(dto.colorHex());
        entidad.setEstado(ESTADO_ACTIVO);

        Set<Long> idsDeseados = idsDetalles(dto.detalles());
        if (!idsDeseados.isEmpty()) {
            Map<Long, ParametroEvaluacion> parametros = cargarYValidar(idsDeseados);
            for (ParametroEvaluacion p : parametros.values()) {
                entidad.getParametros().add(construirEtiquetaParametro(entidad, p));
            }
        }

        // Hibernate hace INSERT etiqueta -> INSERT etiqueta_parametro en cascada
        return mapper.toResponseDto(repository.save(entidad), List.of());
    }

    @Override
    @Transactional
    public EtiquetaClinicaResponseDto actualizar(Long id, EtiquetaClinicaRequestDto dto) {
        bloqueo.adquirir();
        EtiquetaClinica entidad = buscarOFallar(id);

        RevisionAsociaciones.validar(dto.revision(), mapper.toResponseDto(entidad, List.of()).revision());
        if (!entidad.getCodigo().equals(dto.codigo())) {
            throw new BusinessRuleException(
                    "El codigo de una etiqueta no puede modificarse. " +
                    "Si necesita cambiarlo, cree una nueva etiqueta y desactive la actual.");
        }

        entidad.setNombre(dto.nombre());
        entidad.setDescripcion(dto.descripcion());
        entidad.setColorHex(dto.colorHex());

        // DIFF de parametros
        Set<Long> idsDeseados = idsDetalles(dto.detalles());

        Set<Long> idsActuales = entidad.getParametros().stream()
                .map(EtiquetaParametro::getIdParametro)
                .collect(Collectors.toSet());

        // 1. Eliminar los que ya no estan
        entidad.getParametros().removeIf(
                ep -> !idsDeseados.contains(ep.getIdParametro()));

        // 2. Agregar los nuevos
        Set<Long> idsAgregar = new HashSet<>(idsDeseados);
        idsAgregar.removeAll(idsActuales);

        if (!idsAgregar.isEmpty()) {
            Map<Long, ParametroEvaluacion> parametros = cargarYValidar(idsAgregar);
            for (ParametroEvaluacion p : parametros.values()) {
                entidad.getParametros().add(construirEtiquetaParametro(entidad, p));
            }
        }

        EtiquetaClinica guardada = repository.save(entidad);
        return mapper.toResponseDto(
                guardada,
                alertaEtiquetaRepository.findAlertasByEtiqueta(id));
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        bloqueo.adquirir();
        EtiquetaClinica entidad = buscarOFallar(id);
        if (ESTADO_INACTIVO.equals(entidad.getEstado())) {
            throw new BusinessRuleException("La etiqueta ya esta inactiva");
        }
        entidad.setEstado(ESTADO_INACTIVO);
        repository.save(entidad);
    }

    @Override
    @Transactional
    public EtiquetaClinicaResponseDto reactivar(Long id) {
        bloqueo.adquirir();
        EtiquetaClinica entidad = buscarOFallar(id);
        if (ESTADO_ACTIVO.equals(entidad.getEstado())) {
            throw new BusinessRuleException("La etiqueta ya esta activa");
        }
        entidad.setEstado(ESTADO_ACTIVO);
        entidad.setFechaBaja(null);
        EtiquetaClinica guardada = repository.save(entidad);
        return mapper.toResponseDto(
                guardada,
                alertaEtiquetaRepository.findAlertasByEtiqueta(id));
    }

    private EtiquetaClinica buscarOFallar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Etiqueta no encontrada: " + id));
    }

    private Set<Long> idsDetalles(List<upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.ParametroDetalleRequest> detalles) {
        if (detalles == null || detalles.size() > 500) throw new IllegalArgumentException("Detalles obligatorios; maximo 500");
        Set<Long> ids = new LinkedHashSet<>();
        for (var d : detalles) {
            if (d == null || d.idParametro() == null || d.idParametro() <= 0) throw new IllegalArgumentException("Parametro invalido");
            if (!ids.add(d.idParametro())) throw new BusinessRuleException("Parametro duplicado: " + d.idParametro());
        }
        return ids;
    }

    private Map<Long, ParametroEvaluacion> cargarYValidar(Set<Long> ids) {
        List<ParametroEvaluacion> encontrados = parametroRepository.findAllById(ids);
        if (encontrados.size() != ids.size()) {
            throw new BusinessRuleException(
                    "Uno o mas parametros indicados no existen");
        }
        for (ParametroEvaluacion p : encontrados) {
            if (!ESTADO_ACTIVO.equals(p.getEstado())) {
                throw new BusinessRuleException(
                        "El parametro " + p.getCodigo() + " esta inactivo");
            }
        }
        return encontrados.stream()
                .collect(Collectors.toMap(ParametroEvaluacion::getId, p -> p));
    }

    /**
     * Construye el EtiquetaParametro con el patron BomERP:
     * - PK simple autogenerada (no se setea)
     * - @ManyToOne al padre (setEntidad)
     * - FK logica como Long (setIdParametro)
     * - Fotocopia de los datos del parametro
     */
    private EtiquetaParametro construirEtiquetaParametro(
            EtiquetaClinica etiqueta, ParametroEvaluacion parametro) {

        EtiquetaParametro ep = new EtiquetaParametro();
        ep.setEtiqueta(etiqueta);
        ep.setIdParametro(parametro.getId());
        ep.setParametroCodigo(parametro.getCodigo());
        ep.setParametroNombre(parametro.getNombre());
        ep.setParametroUnidad(parametro.getUnidad());
        return ep;
    }
}
