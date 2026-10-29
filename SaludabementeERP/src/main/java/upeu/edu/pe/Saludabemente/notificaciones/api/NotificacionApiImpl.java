package upeu.edu.pe.Saludabemente.notificaciones.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.entity.Notificacion;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.entity.NotificacionDlq;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.repository.NotificacionDlqRepository;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.service.despachadores.DespachadorEmailService;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.service.despachadores.DespachadorInAppService;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.service.plantilla.PlantillaEngineService;
import upeu.edu.pe.Saludabemente.notificaciones.shared.enums.CanalNotificacion;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Despacho de notificaciones sin broker externo.
 *
 * <p>RabbitMQ fue retirado. El envío es síncrono: primero persiste la
 * notificación In-App y, si hay correo, intenta enviarlo. Los fallos de
 * email se registran en la tabla DLQ para el scheduler de reintentos.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionApiImpl implements NotificacionApi {

    private final DespachadorInAppService inAppDespachador;
    private final DespachadorEmailService emailDespachador;
    private final PlantillaEngineService plantillaEngine;
    private final NotificacionDlqRepository dlqRepository;

    @Value("${saludablemente.notificaciones.reintentos.intervalo-minutos:10}")
    private int intervaloReintentoMinutos;

    @Override
    public void enviar(Long idPersona,
                       String codigoEvento,
                       String origenModulo,
                       Long idOrigenReferencia,
                       Map<String, Object> variables,
                       String emailDestino) {

        Notificacion notificacion;
        try {
            notificacion = inAppDespachador.despachar(
                    idPersona, codigoEvento, origenModulo, idOrigenReferencia, variables);
        } catch (Exception ex) {
            log.error("No se pudo registrar la notificación In-App: evento={} persona={}: {}",
                    codigoEvento, idPersona, ex.getMessage(), ex);
            return;
        }

        if (emailDestino != null && !emailDestino.isBlank()) {
            despacharEmailConFallbackADlq(notificacion, codigoEvento, variables, emailDestino);
        }

        log.debug("Notificación procesada: evento={} persona={} origen={}",
                codigoEvento, idPersona, origenModulo);
    }

    private void despacharEmailConFallbackADlq(Notificacion notificacion,
                                               String codigoEvento,
                                               Map<String, Object> variables,
                                               String emailDestino) {
        try {
            emailDespachador.despachar(emailDestino, codigoEvento, variables);
        } catch (RuntimeException ex) {
            log.warn("Email no enviado a {} (evento={}). Persistiendo en DLQ de tabla.",
                    emailDestino, codigoEvento);

            NotificacionDlq dlq = new NotificacionDlq();
            dlq.setIdNotificacion(notificacion.getIdNotificacion());
            dlq.setDestinatario(emailDestino);
            dlq.setCodigoEvento(codigoEvento);
            dlq.setPayload(renderizarPayloadSeguro(codigoEvento, variables));
            dlq.setError(truncar(ex.getMessage(), 500));
            dlq.setIntentos(0);
            dlq.setProximoIntento(LocalDateTime.now().plusMinutes(intervaloReintentoMinutos));
            dlq.setFechaCreacion(LocalDateTime.now());
            dlq.setResuelto(Boolean.FALSE);
            dlqRepository.save(dlq);
        }
    }

    private String renderizarPayloadSeguro(String codigoEvento, Map<String, Object> variables) {
        try {
            return plantillaEngine.renderizarCuerpo(
                    codigoEvento, CanalNotificacion.EMAIL, variables);
        } catch (RuntimeException ex) {
            return "[No se pudo renderizar el cuerpo: " + ex.getMessage() + "]";
        }
    }

    private String truncar(String texto, int max) {
        if (texto == null) return null;
        return texto.length() <= max ? texto : texto.substring(0, max);
    }
}
