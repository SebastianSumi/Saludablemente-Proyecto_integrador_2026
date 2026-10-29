package upeu.edu.pe.Saludabemente.asistencias.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.asistencias.exoneracion.service.ExoneracionService;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AusentismoScheduler {

    private final ExoneracionService exoneracionService;

    /**
     * Todos los días a las 6:00 AM, detecta personas con racha ≥ 3.
     * En Fase 12 se conectará al módulo de Notificaciones.
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void detectarAusentismo() {
        try {
            List<Long> ids = exoneracionService.detectarAusentismoCronico(3);
            if (!ids.isEmpty()) {
                log.warn("Ausentismo crónico detectado: {} personas {}", ids.size(), ids);
                // TODO: en Fase 12 disparar evento a Notificaciones
            }
        } catch (Exception e) {
            log.error("Error en scheduler de ausentismo", e);
        }
    }
}