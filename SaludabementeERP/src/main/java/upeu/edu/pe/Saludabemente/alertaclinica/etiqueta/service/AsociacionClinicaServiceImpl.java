package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.AlertaClinica;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.repository.AlertaClinicaRepository;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.*;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.entity.*;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.mapper.EtiquetaClinicaMapper;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.repository.*;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.parametro.entity.ParametroEvaluacion;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.parametro.repository.ParametroEvaluacionRepository;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

import java.util.*;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AsociacionClinicaServiceImpl implements AsociacionClinicaService {
    private final EtiquetaClinicaRepository etiquetas;
    private final ParametroEvaluacionRepository parametros;
    private final AlertaClinicaRepository alertas;
    private final AlertaEtiquetaRepository asociaciones;
    private final EtiquetaClinicaMapper mapper;
    private final BloqueoAsociaciones bloqueo;

    @Override
    public AlertaEtiquetasResponse etiquetasDeAlerta(Long id) {
        alerta(id);
        var detalles = asociaciones.findEtiquetasByAlerta(id);
        return new AlertaEtiquetasResponse(id, revision("alerta", id, detalles), detalles);
    }

    @Override
    @Transactional
    public AlertaEtiquetasResponse guardarEtiquetasDeAlerta(Long id, GuardarEtiquetasRequest request) {
        bloqueo.adquirir();
        var alerta = alerta(id);
        RevisionAsociaciones.validar(request.revision(), etiquetasDeAlerta(id).revision());
        var deseados = ids(request.detalles(), EtiquetaDetalleRequest::idEtiqueta);
        var actuales = new LinkedHashMap<Long, AlertaEtiqueta>();
        alerta.getEtiquetas().forEach(a -> actuales.put(a.getIdEtiqueta(), a));
        // Validar la operacion completa antes de mutar la coleccion.
        var nuevas = new ArrayList<EtiquetaClinica>();
        for (Long etiquetaId : deseados) {
            if (!actuales.containsKey(etiquetaId)) {
                var etiqueta = etiqueta(etiquetaId);
                activa(etiqueta);
                nuevas.add(etiqueta);
            }
        }
        actuales.values().stream().filter(a -> !deseados.contains(a.getIdEtiqueta())).forEach(this::editable);
        alerta.getEtiquetas().removeIf(a -> !deseados.contains(a.getIdEtiqueta()));
        nuevas.forEach(e -> alerta.getEtiquetas().add(nueva(alerta, e)));
        alertas.saveAndFlush(alerta);
        return etiquetasDeAlerta(id);
    }

    @Override
    public EtiquetaAlertasResponse alertasDeEtiqueta(Long id) {
        etiqueta(id);
        var detalles = asociaciones.findAlertasByEtiqueta(id);
        return new EtiquetaAlertasResponse(id, revision("etiqueta-alertas", id, detalles), detalles);
    }

    @Override
    @Transactional
    public EtiquetaAlertasResponse guardarAlertasDeEtiqueta(Long id, GuardarAlertasRequest request) {
        bloqueo.adquirir();
        var etiqueta = etiqueta(id);
        RevisionAsociaciones.validar(request.revision(), alertasDeEtiqueta(id).revision());
        var deseados = ids(request.detalles(), AlertaDetalleRequest::idAlerta);
        var actuales = new LinkedHashMap<Long, AlertaEtiqueta>();
        asociaciones.findByIdEtiqueta(id).forEach(a -> actuales.put(a.getAlerta().getIdAlerta(), a));
        var nuevas = new ArrayList<AlertaClinica>();
        for (Long alertaId : deseados) {
            if (!actuales.containsKey(alertaId)) {
                activa(etiqueta);
                nuevas.add(alerta(alertaId));
            }
        }
        var retiradas = actuales.entrySet().stream().filter(e -> !deseados.contains(e.getKey()))
                .map(Map.Entry::getValue).toList();
        retiradas.forEach(this::editable);
        // Siempre mutar desde el agregado propietario, tambien en la vista inversa.
        retiradas.forEach(a -> a.getAlerta().getEtiquetas().remove(a));
        nuevas.forEach(a -> a.getEtiquetas().add(nueva(a, etiqueta)));
        alertas.flush();
        return alertasDeEtiqueta(id);
    }

    @Override
    public ParametroEtiquetasResponse etiquetasDeParametro(Long id) {
        parametro(id);
        var detalles = etiquetas.buscarResumen(null, null, id);
        // La revision de esta tarjeta solo depende de sus asociaciones, no de contadores de otras tarjetas.
        var claves = detalles.stream().map(EtiquetaResumenDto::id).sorted().toList();
        return new ParametroEtiquetasResponse(id, revision("parametro", id, claves), detalles);
    }

    @Override
    @Transactional
    public ParametroEtiquetasResponse guardarEtiquetasDeParametro(Long id, GuardarEtiquetasRequest request) {
        bloqueo.adquirir();
        var parametro = parametro(id);
        RevisionAsociaciones.validar(request.revision(), etiquetasDeParametro(id).revision());
        var deseados = ids(request.detalles(), EtiquetaDetalleRequest::idEtiqueta);
        var catalogo = etiquetas.findAllConParametros();
        var actuales = new HashSet<Long>();
        catalogo.stream().filter(e -> e.getParametros().stream().anyMatch(p -> id.equals(p.getIdParametro())))
                .forEach(e -> actuales.add(e.getId()));
        var nuevas = new ArrayList<EtiquetaClinica>();
        for (Long etiquetaId : deseados) {
            if (!actuales.contains(etiquetaId)) {
                var etiqueta = etiqueta(etiquetaId);
                activa(etiqueta);
                parametroActivo(parametro);
                nuevas.add(etiqueta);
            }
        }
        catalogo.stream().filter(e -> actuales.contains(e.getId()) && !deseados.contains(e.getId()))
                .forEach(e -> e.getParametros().removeIf(p -> id.equals(p.getIdParametro())));
        nuevas.forEach(e -> e.getParametros().add(nuevoParametro(e, parametro)));
        etiquetas.flush();
        return etiquetasDeParametro(id);
    }

    @Override
    public EtiquetaParametrosResponse parametrosDeEtiqueta(Long id) {
        var detalles = mapper.toResponseDto(etiqueta(id), List.of()).parametros();
        return new EtiquetaParametrosResponse(id, revision("etiqueta-parametros", id, detalles), detalles);
    }

    @Override
    @Transactional
    public EtiquetaParametrosResponse guardarParametrosDeEtiqueta(Long id, GuardarParametrosRequest request) {
        bloqueo.adquirir();
        var etiqueta = etiqueta(id);
        RevisionAsociaciones.validar(request.revision(), parametrosDeEtiqueta(id).revision());
        var deseados = ids(request.detalles(), ParametroDetalleRequest::idParametro);
        var actuales = new HashSet<Long>();
        etiqueta.getParametros().forEach(p -> actuales.add(p.getIdParametro()));
        var nuevos = new ArrayList<ParametroEvaluacion>();
        for (Long parametroId : deseados) {
            if (!actuales.contains(parametroId)) {
                var parametro = parametro(parametroId);
                parametroActivo(parametro);
                nuevos.add(parametro);
            }
        }
        etiqueta.getParametros().removeIf(p -> !deseados.contains(p.getIdParametro()));
        nuevos.forEach(p -> etiqueta.getParametros().add(nuevoParametro(etiqueta, p)));
        etiquetas.saveAndFlush(etiqueta);
        return parametrosDeEtiqueta(id);
    }

    private EtiquetaClinica etiqueta(Long id) {
        return etiquetas.findById(id).orElseThrow(() -> new ResourceNotFoundException("Etiqueta no encontrada: " + id));
    }

    private AlertaClinica alerta(Long id) {
        return alertas.findById(id).orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada: " + id));
    }

    private ParametroEvaluacion parametro(Long id) {
        return parametros.findById(id).orElseThrow(() -> new ResourceNotFoundException("Parametro no encontrado: " + id));
    }

    private void activa(EtiquetaClinica etiqueta) {
        if (!"ACTIVO".equals(etiqueta.getEstado())) throw new BusinessRuleException("La etiqueta esta inactiva: " + etiqueta.getCodigo());
    }

    private void parametroActivo(ParametroEvaluacion parametro) {
        if (!"ACTIVO".equals(parametro.getEstado())) throw new BusinessRuleException("El parametro esta inactivo: " + parametro.getCodigo());
    }

    private void editable(AlertaEtiqueta asociacion) {
        if (!"MANUAL".equals(asociacion.getAsignadaPor())) throw new BusinessRuleException("No se pueden retirar asociaciones automaticas MOTOR");
    }

    private AlertaEtiqueta nueva(AlertaClinica alerta, EtiquetaClinica etiqueta) {
        var asociacion = new AlertaEtiqueta();
        asociacion.setAlerta(alerta);
        asociacion.setIdEtiqueta(etiqueta.getId());
        asociacion.setEtiquetaCodigo(etiqueta.getCodigo());
        asociacion.setEtiquetaNombre(etiqueta.getNombre());
        asociacion.setEtiquetaColorHex(etiqueta.getColorHex());
        asociacion.setAsignadaPor("MANUAL");
        return asociacion;
    }

    private EtiquetaParametro nuevoParametro(EtiquetaClinica etiqueta, ParametroEvaluacion parametro) {
        var asociacion = new EtiquetaParametro();
        asociacion.setEtiqueta(etiqueta);
        asociacion.setIdParametro(parametro.getId());
        asociacion.setParametroCodigo(parametro.getCodigo());
        asociacion.setParametroNombre(parametro.getNombre());
        asociacion.setParametroUnidad(parametro.getUnidad());
        return asociacion;
    }

    private String revision(String tipo, Long id, List<?> detalles) {
        // Orden canonico: la revision no depende del orden de filas devuelto por SQL.
        return RevisionAsociaciones.calcular(List.of(tipo, id, detalles.stream().map(Object::toString).sorted().toList()));
    }

    private <T> Set<Long> ids(List<T> detalles, Function<T, Long> extraer) {
        if (detalles == null || detalles.size() > 500) throw new IllegalArgumentException("Detalles obligatorios; maximo 500");
        Set<Long> resultado = new LinkedHashSet<>();
        for (T detalle : detalles) {
            Long id = detalle == null ? null : extraer.apply(detalle);
            if (id == null || id <= 0) throw new IllegalArgumentException("Identificador de detalle invalido");
            if (!resultado.add(id)) throw new BusinessRuleException("Detalle duplicado: " + id);
        }
        return resultado;
    }
}
