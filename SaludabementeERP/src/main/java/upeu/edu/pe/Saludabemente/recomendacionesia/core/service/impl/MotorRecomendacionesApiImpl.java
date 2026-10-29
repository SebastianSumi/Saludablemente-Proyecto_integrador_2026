package upeu.edu.pe.Saludabemente.recomendacionesia.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.recomendacionesia.api.MotorRecomendacionesApi;
import upeu.edu.pe.Saludabemente.recomendacionesia.config.RecomendacionesIaProperties;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.entity.EstadoPendiente;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.entity.RecomendacionPendiente;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.repository.RecomendacionIaRepository;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.repository.RecomendacionPendienteRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MotorRecomendacionesApiImpl implements MotorRecomendacionesApi {

    private final RecomendacionPendienteRepository pendienteRepository;
    private final RecomendacionIaRepository recomendacionRepository;
    private final RecomendacionesIaProperties properties;

    @Override
    @Transactional
    public void dispararGeneracion(Long idEvaluacion, Long idPersona) {

        if (idEvaluacion == null || idPersona == null) {
            log.warn("dispararGeneracion llamado con argumentos nulos. Ignorado.");
            return;
        }

        boolean yaPendiente = pendienteRepository.existsByIdPersonaAndIdEvaluacionAndEstadoIn(
                idPersona, idEvaluacion,
                List.of(EstadoPendiente.PENDIENTE, EstadoPendiente.PROCESANDO));

        if (yaPendiente) {
            log.info("Ya existe pendiente activo para persona={} evaluacion={}. Ignorado.",
                    idPersona, idEvaluacion);
            return;
        }

        boolean yaVigente = recomendacionRepository.existsByIdPersonaAndIdEvaluacionAndVigente(
                idPersona, idEvaluacion, 1);

        if (yaVigente) {
            log.info("Ya existe recomendación vigente para persona={} evaluacion={}. Ignorado.",
                    idPersona, idEvaluacion);
            return;
        }

        RecomendacionPendiente pendiente = new RecomendacionPendiente();
        pendiente.setIdPersona(idPersona);
        pendiente.setIdEvaluacion(idEvaluacion);
        pendiente.setEstado(EstadoPendiente.PENDIENTE);
        pendiente.setIntentos(0);
        pendiente.setMaxIntentos(properties.getOutbox().getMaxIntentos());
        pendiente.setFechaCreacion(LocalDateTime.now());

        pendienteRepository.save(pendiente);

        log.info("Pendiente creado id={} persona={} evaluacion={}",
                pendiente.getIdPendiente(), idPersona, idEvaluacion);
    }
}