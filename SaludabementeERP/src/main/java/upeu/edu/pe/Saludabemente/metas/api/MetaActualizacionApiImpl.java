package upeu.edu.pe.Saludabemente.metas.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.metas.api.dto.ResultadoActualizacionDTO;
import upeu.edu.pe.Saludabemente.metas.meta.entity.MetaEntity;
import upeu.edu.pe.Saludabemente.metas.meta.repository.MetaRepository;
import upeu.edu.pe.Saludabemente.metas.meta.service.AuditoriaMetasService;
import upeu.edu.pe.Saludabemente.metas.meta.service.NotificacionMetasService;
import upeu.edu.pe.Saludabemente.metas.meta.service.impl.MetaServiceImpl;
import upeu.edu.pe.Saludabemente.metas.shared.enums.EstadoMeta;
import upeu.edu.pe.Saludabemente.metas.shared.enums.TipoCalculo;
import upeu.edu.pe.Saludabemente.personal.api.PersonalNotificacionesApiService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
class MetaActualizacionApiImpl implements MetaActualizacionApi {

    private final MetaRepository metaRepository;
    private final PersonalNotificacionesApiService personaApi;
    private final AuditoriaMetasService auditoriaMetas;
    private final NotificacionMetasService notificacionMetas;

    @Override
    public ResultadoActualizacionDTO actualizarDesdeEvaluacion(
            Long idPersona,
            Long idEvaluacion,
            Map<String, BigDecimal> indicadoresActualizados) {

        // 1. Validacion de parametros
        if (idPersona == null) {
            throw new IllegalArgumentException("idPersona no puede ser nulo");
        }
        if (idEvaluacion == null) {
            throw new IllegalArgumentException("idEvaluacion no puede ser nulo");
        }
        if (indicadoresActualizados == null || indicadoresActualizados.isEmpty()) {
            throw new IllegalArgumentException(
                    "indicadoresActualizados no puede ser nulo ni vacio");
        }

        // 2. Validar persona activa
        if (!personaApi.existeYEstaActiva(idPersona)) {
            throw new ResourceNotFoundException(
                    "Persona no encontrada o inactiva: " + idPersona);
        }

        // 3. Candidatas: metas ACTIVAS de la persona
        List<MetaEntity> candidatas = metaRepository
                .findByIdPersonaAndEstado(idPersona, EstadoMeta.ACTIVA);

        if (candidatas.isEmpty()) {
            log.debug("[MetaActualizacion] Sin metas activas para persona {}", idPersona);
            return new ResultadoActualizacionDTO(
                    idPersona, idEvaluacion, 0, 0, 0, List.of());
        }

        // 4. Filtrar por tipoCalculo ULTIMO_VALOR e indicador presente en el Map
        List<MetaEntity> aProcesar = candidatas.stream()
                .filter(m -> m.getTipoCalculo() == TipoCalculo.ULTIMO_VALOR)
                .filter(m -> indicadoresActualizados.containsKey(m.getIndicador()))
                .toList();

        if (aProcesar.isEmpty()) {
            log.debug("[MetaActualizacion] Sin metas coincidentes para persona {}",
                    idPersona);
            return new ResultadoActualizacionDTO(
                    idPersona, idEvaluacion, 0, 0, 0, List.of());
        }

        // 5. Actualizar cada meta
        int actualizadas = 0;
        int completadas = 0;
        List<Long> afectadas = new ArrayList<>();

        for (MetaEntity meta : aProcesar) {
            BigDecimal valorNuevo = indicadoresActualizados.get(meta.getIndicador());
            BigDecimal valorAnterior = meta.getValorActual();

            // Idempotencia: si el valor es el mismo, no se toca nada
            if (valorAnterior != null
                    && valorAnterior.compareTo(valorNuevo) == 0) {
                continue;
            }

            meta.setValorActual(valorNuevo);
            meta.setModificadoPor("nutricional-sync");

            // Reevaluar estado
            BigDecimal avance = MetaServiceImpl.calcularAvance(meta);
            boolean seCompleto = false;
            if (avance.compareTo(BigDecimal.valueOf(100)) >= 0
                    && meta.getEstado() != EstadoMeta.COMPLETADA) {
                meta.setEstado(EstadoMeta.COMPLETADA);
                meta.setFechaCierre(LocalDateTime.now());
                seCompleto = true;
                completadas++;
            }

            metaRepository.save(meta);
            actualizadas++;
            afectadas.add(meta.getId());

            // Auditar (tolerante a fallo)
            try {
                auditoriaMetas.registrarActualizacionDesdeEvaluacion(
                        meta.getId(),
                        idEvaluacion,
                        valorAnterior == null ? null : valorAnterior.toPlainString(),
                        valorNuevo.toPlainString());
            } catch (RuntimeException ex) {
                log.warn("[MetaActualizacion] Fallo al auditar meta {}: {}",
                        meta.getId(), ex.getMessage());
            }

            // Notificar si se completo (tolerante a fallo)
            if (seCompleto) {
                try {
                    notificacionMetas.notificarMetaCompletada(
                            meta.getIdPersona(),
                            meta.getId(),
                            meta.getDescripcion());
                } catch (RuntimeException ex) {
                    log.warn("[MetaActualizacion] Fallo al notificar meta {}: {}",
                            meta.getId(), ex.getMessage());
                }
            }
        }

        log.info("[MetaActualizacion] persona={} evaluacion={} evaluadas={} "
                        + "actualizadas={} completadas={}",
                idPersona, idEvaluacion, aProcesar.size(), actualizadas, completadas);

        return new ResultadoActualizacionDTO(
                idPersona,
                idEvaluacion,
                aProcesar.size(),
                actualizadas,
                completadas,
                List.copyOf(afectadas));
    }
}