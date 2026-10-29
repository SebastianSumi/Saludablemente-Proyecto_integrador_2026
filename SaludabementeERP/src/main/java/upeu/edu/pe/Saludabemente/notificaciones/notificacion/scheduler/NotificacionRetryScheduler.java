package upeu.edu.pe.Saludabemente.notificaciones.notificacion.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.entity.NotificacionDlq;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.repository.NotificacionDlqRepository;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.service.despachadores.DespachadorEmailService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacionRetryScheduler {

    private final NotificacionDlqRepository dlqRepository;
    private final DespachadorEmailService emailDespachador;

    @Value("${saludablemente.notificaciones.reintentos.max-intentos:5}")
    private int maxIntentos;

    @Value("${saludablemente.notificaciones.reintentos.intervalo-minutos:10}")
    private int intervaloMinutos;

    @Value("${saludablemente.notificaciones.reintentos.backoff-multiplicador:2}")
    private int backoffMultiplicador;

    @Scheduled(fixedDelayString = "${saludablemente.notificaciones.reintentos.intervalo-minutos:10}000")
    @Transactional
    public void reintentarFallidos() {
        List<NotificacionDlq> pendientes = dlqRepository
                .findByResueltoFalseAndProximoIntentoBeforeAndIntentosLessThan(
                        LocalDateTime.now(), maxIntentos);

        if (pendientes.isEmpty()) {
            return;
        }

        log.info("Reintentando {} notificaciones fallidas", pendientes.size());

        for (NotificacionDlq dlq : pendientes) {
            procesarUno(dlq);
        }
    }

    private void procesarUno(NotificacionDlq dlq) {
        try {
            // El payload guarda el HTML ya renderizado: se reenvía tal cual,
            // sin volver a consultar la plantilla (que pudo haber cambiado).
            emailDespachador.despacharPayloadCrudo(dlq.getDestinatario(), dlq.getPayload());
            dlq.setResuelto(Boolean.TRUE);
            dlqRepository.save(dlq);
            log.info("Reintento exitoso para DLQ {} (destinatario={})",
                    dlq.getIdDlq(), dlq.getDestinatario());
        } catch (Exception e) {
            int nuevosIntentos = dlq.getIntentos() + 1;
            dlq.setIntentos(nuevosIntentos);
            dlq.setError(truncar(e.getMessage(), 500));

            if (nuevosIntentos >= maxIntentos) {
                dlq.setProximoIntento(null);
                log.error("DLQ {} agotó {} intentos. Requiere revisión manual. Error: {}",
                        dlq.getIdDlq(), maxIntentos, e.getMessage());
            } else {
                long minutosEspera = (long) intervaloMinutos
                        * (long) Math.pow(backoffMultiplicador, nuevosIntentos);
                dlq.setProximoIntento(LocalDateTime.now().plusMinutes(minutosEspera));
                log.warn("Reintento {} de {} falló para DLQ {}. Próximo intento en {} min",
                        nuevosIntentos, maxIntentos, dlq.getIdDlq(), minutosEspera);
            }
            dlqRepository.save(dlq);
        }
    }

    private String truncar(String texto, int max) {
        if (texto == null) return null;
        return texto.length() <= max ? texto : texto.substring(0, max);
    }
}