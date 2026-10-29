package upeu.edu.pe.Saludabemente.alertaclinica.alerta.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto.AlertaAgregado;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto.AlertaClinicaResumen;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto.AlertaClinicaResponse;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto.AlertaReporte;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto.AtenderAlertaRequest;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.AlertaClinica;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.EstadoAlerta;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.SeveridadAlerta;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.TipoEvaluacionOrigen;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.mapper.AlertaClinicaMapper;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.repository.AlertaClinicaRepository;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.EtiquetaAgregadoDto;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.EtiquetaAsignadaDto;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.entity.AlertaEtiqueta;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.entity.EtiquetaClinica;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.repository.AlertaEtiquetaRepository;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.repository.EtiquetaClinicaRepository;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertaClinicaServiceImpl implements AlertaClinicaService {

    private static final String ETIQUETA_ACTIVA = "ACTIVO";

    private final AlertaClinicaRepository alertaRepository;
    private final AlertaClinicaMapper alertaMapper;
    private final AlertaEtiquetaRepository alertaEtiquetaRepository;
    private final EtiquetaClinicaRepository etiquetaClinicaRepository;
    private final upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.service.BloqueoAsociaciones bloqueo;

    @Override
    @Transactional(readOnly = true)
    public List<AlertaClinicaResponse> buscar(
            EstadoAlerta estado,
            SeveridadAlerta severidad,
            Long personaId,
            TipoEvaluacionOrigen tipoOrigen,
            LocalDateTime desde,
            LocalDateTime hasta,
            List<String> codigosEtiquetas,
            String ordenarPor,
            String direccion) {

        if (desde != null && hasta != null && desde.isAfter(hasta)) throw new IllegalArgumentException("El rango de fechas es invalido");
        if (!Set.of("fechaGeneracion", "nivelSeveridad", "estado", "puntajeTotal", "idAlerta").contains(ordenarPor))
            throw new IllegalArgumentException("Campo de ordenamiento invalido");
        if (!"ASC".equalsIgnoreCase(direccion) && !"DESC".equalsIgnoreCase(direccion))
            throw new IllegalArgumentException("La direccion debe ser ASC o DESC");
        Sort.Direction dir = "ASC".equalsIgnoreCase(direccion)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Sort sort = Sort.by(dir, ordenarPor);

        List<AlertaClinica> alertas;

        if (codigosEtiquetas == null || codigosEtiquetas.isEmpty()) {
            alertas = alertaRepository.buscar(
                    estado, severidad, personaId, tipoOrigen, desde, hasta, sort);
        } else {
            Set<String> codigosNormalizados = codigosEtiquetas.stream()
                    .filter(c -> c != null && !c.isBlank())
                    .map(c -> c.trim().toUpperCase())
                    .collect(Collectors.toSet());

            alertas = alertaRepository.buscarPorEtiquetas(
                    estado, severidad, personaId, tipoOrigen, desde, hasta,
                    codigosNormalizados, sort);
        }

        return alertaMapper.toResponseList(alertas);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<AlertaClinicaResumen> buscarOpciones(
            Long personaId, EstadoAlerta estado, SeveridadAlerta severidad, LocalDateTime desde,
            LocalDateTime hasta, int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 1 || tamanio > 100) throw new IllegalArgumentException("Pagina >= 0 y tamanio entre 1 y 100");
        if (desde != null && hasta != null && desde.isAfter(hasta)) throw new IllegalArgumentException("El rango de fechas es invalido");
        return alertaRepository.buscarOpciones(personaId, estado, severidad, desde, hasta,
                org.springframework.data.domain.PageRequest.of(pagina, tamanio));
    }

    @Override
    @Transactional(readOnly = true)
    public AlertaClinicaResponse obtener(Long id) {
        return alertaMapper.toResponse(buscarOFallar(id));
    }

    @Override
    @Transactional
    public AlertaClinicaResponse atender(Long id, AtenderAlertaRequest request) {
        AlertaClinica alerta = buscarOFallar(id);

        if (alerta.getEstado() != EstadoAlerta.PENDIENTE) {
            throw new BusinessRuleException(
                    "Solo se pueden atender alertas en estado PENDIENTE. Estado actual: "
                            + alerta.getEstado());
        }

        alerta.setEstado(EstadoAlerta.ATENDIDA);
        alerta.setFechaAtencion(LocalDateTime.now());
        alerta.setUsuarioAtiendeId(request.idUsuario());
        alerta.setObservacionAtencion(request.observacion());

        return alertaMapper.toResponse(alertaRepository.save(alerta));
    }

    @Override
    @Transactional
    public AlertaClinicaResponse anular(Long id) {
        AlertaClinica alerta = buscarOFallar(id);

        if (alerta.getEstado() == EstadoAlerta.ANULADA) {
            throw new BusinessRuleException("La alerta ya esta anulada");
        }

        alerta.setEstado(EstadoAlerta.ANULADA);
        return alertaMapper.toResponse(alertaRepository.save(alerta));
    }

    @Override
    @Transactional(readOnly = true)
    public AlertaReporte reporte(LocalDateTime desde, LocalDateTime hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) throw new IllegalArgumentException("El rango de fechas es invalido");
        AlertaAgregado agregado = alertaRepository.agregados(desde, hasta);

        Sort sort = Sort.by(Sort.Direction.DESC, "fechaGeneracion");
        List<AlertaClinicaResumen> alertas = alertaRepository.buscarResumen(
                null, desde, hasta, sort);

        return new AlertaReporte(agregado, alertas);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtiquetaAgregadoDto> reportePorEtiqueta(LocalDateTime desde, LocalDateTime hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) throw new IllegalArgumentException("El rango de fechas es invalido");
        return alertaEtiquetaRepository.countAlertasPorEtiqueta(desde, hasta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtiquetaAsignadaDto> listarEtiquetas(Long idAlerta) {
        buscarOFallar(idAlerta);
        return alertaEtiquetaRepository.findEtiquetasByAlerta(idAlerta);
    }

    @Override
    @Transactional
    public List<EtiquetaAsignadaDto> agregarEtiqueta(Long idAlerta, Long idEtiqueta) {
        bloqueo.adquirir();
        AlertaClinica alerta = buscarOFallar(idAlerta);

        EtiquetaClinica etiqueta = etiquetaClinicaRepository.findById(idEtiqueta)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Etiqueta no encontrada: " + idEtiqueta));

        if (!ETIQUETA_ACTIVA.equals(etiqueta.getEstado())) {
            throw new BusinessRuleException(
                    "No se puede asignar una etiqueta inactiva: " + etiqueta.getCodigo());
        }

        if (alertaEtiquetaRepository.existsByAlerta_IdAlertaAndIdEtiqueta(idAlerta, idEtiqueta)) {
            throw new BusinessRuleException(
                    "La etiqueta " + etiqueta.getCodigo() + " ya esta asignada a esta alerta");
        }

        AlertaEtiqueta ae = new AlertaEtiqueta();
        ae.setAlerta(alerta);
        ae.setIdEtiqueta(etiqueta.getId());
        ae.setEtiquetaCodigo(etiqueta.getCodigo());
        ae.setEtiquetaNombre(etiqueta.getNombre());
        ae.setEtiquetaColorHex(etiqueta.getColorHex());
        ae.setAsignadaPor("MANUAL");

        alertaEtiquetaRepository.save(ae);

        return alertaEtiquetaRepository.findEtiquetasByAlerta(idAlerta);
    }

    @Override
    @Transactional
    public List<EtiquetaAsignadaDto> quitarEtiqueta(Long idAlerta, Long idEtiqueta) {
        bloqueo.adquirir();
        AlertaClinica alerta = buscarOFallar(idAlerta);

        AlertaEtiqueta asignada = alerta.getEtiquetas().stream()
                .filter(ae -> idEtiqueta.equals(ae.getIdEtiqueta()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La etiqueta " + idEtiqueta + " no esta asignada a la alerta " + idAlerta));

        if (!"MANUAL".equals(asignada.getAsignadaPor()))
            throw new BusinessRuleException("No se pueden retirar etiquetas automaticas MOTOR");
        alerta.getEtiquetas().remove(asignada);
        alertaRepository.flush();

        return alertaEtiquetaRepository.findEtiquetasByAlerta(idAlerta);
    }

    private AlertaClinica buscarOFallar(Long id) {
        return alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "AlertaClinica no encontrada: " + id));
    }
}