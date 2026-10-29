package upeu.edu.pe.Saludabemente.recomendacionesia.core.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.entity.EstadoPendiente;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.entity.RecomendacionPendiente;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.repository.RecomendacionPendienteRepository;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.service.OrquestadorRecomendacionService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private static final int LOTE = 10;

    private final RecomendacionPendienteRepository pendienteRepository;
    private final OrquestadorRecomendacionService orquestador;

    /**
     * Corre cada N milisegundos (configurable en YAML, por defecto 5 min).
     * Toma hasta LOTE pendientes y los procesa uno por uno.
     */
    @Scheduled(fixedDelayString = "${saludablemente.recomendaciones.outbox.scheduler-interval-ms:300000}")
    public void procesarPendientes() {
        List<RecomendacionPendiente> pendientes = pendienteRepository
                .findByEstadoOrderByFechaCreacionAsc(
                        EstadoPendiente.PENDIENTE,
                        PageRequest.of(0, LOTE));

        if (pendientes.isEmpty()) {
            return;
        }

        log.info("Procesando {} pendiente(s) del outbox", pendientes.size());

        for (RecomendacionPendiente pendiente : pendientes) {
            try {
                orquestador.procesarPendiente(pendiente);
            } catch (Exception ex) {
                log.error("Error inesperado procesando pendiente {}: {}",
                        pendiente.getIdPendiente(), ex.getMessage(), ex);
            }
        }
    }
}