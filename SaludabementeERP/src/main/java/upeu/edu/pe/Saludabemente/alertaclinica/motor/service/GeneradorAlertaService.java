package upeu.edu.pe.Saludabemente.alertaclinica.motor.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.AlertaClinica;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.AlertaClinicaDetalle;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.EstadoAlerta;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.SeveridadAlerta;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.TipoEvaluacionOrigen;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.mapper.AlertaClinicaMapper;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.repository.AlertaClinicaRepository;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.service.ClasificadorSeveridad;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.entity.AlertaEtiqueta;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.entity.EtiquetaClinica;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.repository.EtiquetaClinicaRepository;
import upeu.edu.pe.Saludabemente.alertaclinica.integracion.dto.Hallazgo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeneradorAlertaService {

    private static final Logger log = LoggerFactory.getLogger(GeneradorAlertaService.class);
    private static final String ASIGNADA_POR_MOTOR = "MOTOR";

    private final AlertaClinicaRepository alertaRepository;
    private final AlertaClinicaMapper alertaMapper;
    private final ClasificadorSeveridad clasificadorSeveridad;
    private final EtiquetaClinicaRepository etiquetaClinicaRepository;
    private final upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.service.BloqueoAsociaciones bloqueo;

    @Transactional
    public void generar(
            Long personaId,
            Long evaluacionOrigenId,
            TipoEvaluacionOrigen tipoEvaluacionOrigen,
            List<Hallazgo> hallazgos) {

        bloqueo.adquirir();
        // 1. Idempotencia
        if (alertaRepository
                .findByEvaluacionOrigenIdAndTipoEvaluacionOrigen(
                        evaluacionOrigenId, tipoEvaluacionOrigen)
                .isPresent()) {
            log.debug("Ya existe alerta para evaluacion {} de tipo {}. Se omite.",
                    evaluacionOrigenId, tipoEvaluacionOrigen);
            return;
        }

        // 2. Filtrar hallazgos con puntos
        List<Hallazgo> hallazgosConPuntos = hallazgos.stream()
                .filter(h -> h.puntosRiesgo() != null && h.puntosRiesgo() > 0)
                .toList();

        if (hallazgosConPuntos.isEmpty()) {
            log.debug("Evaluacion {} de tipo {} sin hallazgos con puntos. No se genera alerta.",
                    evaluacionOrigenId, tipoEvaluacionOrigen);
            return;
        }

        // 3. Sumar puntos y clasificar severidad
        int puntajeTotal = hallazgosConPuntos.stream()
                .mapToInt(Hallazgo::puntosRiesgo)
                .sum();

        SeveridadAlerta severidad = clasificadorSeveridad.clasificar(puntajeTotal);

        // 4. Construir cabecera
        AlertaClinica alerta = new AlertaClinica();
        alerta.setPersonaId(personaId);
        alerta.setEvaluacionOrigenId(evaluacionOrigenId);
        alerta.setTipoEvaluacionOrigen(tipoEvaluacionOrigen);
        alerta.setPuntajeTotal(puntajeTotal);
        alerta.setNivelSeveridad(severidad);
        alerta.setEstado(EstadoAlerta.PENDIENTE);
        alerta.setFechaGeneracion(LocalDateTime.now());
        alerta.setMensajeResumen(
                buildMensajeResumen(hallazgosConPuntos.size(), puntajeTotal, severidad));

        // 5. Construir detalles
        for (Hallazgo h : hallazgosConPuntos) {
            AlertaClinicaDetalle detalle = alertaMapper.toDetalle(h);
            detalle.setAlerta(alerta);
            alerta.getDetalles().add(detalle);
        }

        // 6. Asignar etiquetas automaticas ANTES de persistir
        asignarEtiquetasAutomaticas(alerta, hallazgosConPuntos);

        // 7. Persistir todo en cascada (alerta + detalles + etiquetas)
        AlertaClinica guardada = alertaRepository.save(alerta);

        log.info("Alerta {} generada para persona {}: {} indicadores, {} puntos, severidad {}, {} etiquetas",
                guardada.getIdAlerta(),
                personaId,
                hallazgosConPuntos.size(),
                puntajeTotal,
                severidad,
                guardada.getEtiquetas().size());
    }

    /**
     * Asigna automaticamente las etiquetas activas cuyos parametros
     * coincidan con al menos un tipoIndicador de los hallazgos.
     *
     * Usa asignadaPor = "MOTOR" y fotocopia de los datos de la etiqueta.
     */
    private void asignarEtiquetasAutomaticas(AlertaClinica alerta, List<Hallazgo> hallazgos) {

        // Extraer codigos de parametros (tipoIndicador) de los hallazgos
        Set<String> codigosParametros = hallazgos.stream()
                .map(Hallazgo::tipoIndicador)
                .filter(Objects::nonNull)
                .filter(c -> !c.isBlank())
                .collect(Collectors.toSet());

        // Proteccion: Oracle falla con IN () vacio
        if (codigosParametros.isEmpty()) {
            return;
        }

        // Buscar etiquetas activas que tengan al menos uno de esos parametros
        List<EtiquetaClinica> etiquetas = etiquetaClinicaRepository
                .findActivasPorCodigosParametros(codigosParametros);

        if (etiquetas.isEmpty()) {
            log.debug("No hay etiquetas activas para los parametros: {}", codigosParametros);
            return;
        }

        // Construir las AlertaEtiqueta con fotocopia
        for (EtiquetaClinica etiqueta : etiquetas) {
            AlertaEtiqueta ae = new AlertaEtiqueta();
            ae.setAlerta(alerta);
            ae.setIdEtiqueta(etiqueta.getId());
            ae.setEtiquetaCodigo(etiqueta.getCodigo());
            ae.setEtiquetaNombre(etiqueta.getNombre());
            ae.setEtiquetaColorHex(etiqueta.getColorHex());
            ae.setAsignadaPor(ASIGNADA_POR_MOTOR);
            // fechaAsignacion y asignadaPor se refuerzan en @PrePersist

            alerta.getEtiquetas().add(ae);
        }

        log.debug("Asignadas {} etiquetas automaticas a la alerta: {}",
                etiquetas.size(),
                etiquetas.stream().map(EtiquetaClinica::getCodigo).toList());
    }

    private String buildMensajeResumen(int cantidadIndicadores, int puntajeTotal,
                                       SeveridadAlerta severidad) {
        return String.format(
                "%d indicador(es) alterado(s). Puntaje %d, severidad %s",
                cantidadIndicadores, puntajeTotal, severidad);
    }
}