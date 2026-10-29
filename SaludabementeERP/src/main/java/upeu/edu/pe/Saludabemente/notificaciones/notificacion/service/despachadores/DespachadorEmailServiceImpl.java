package upeu.edu.pe.Saludabemente.notificaciones.notificacion.service.despachadores;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import upeu.edu.pe.Saludabemente.exception.DeliveryFallidoException;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.service.plantilla.PlantillaEngineService;
import upeu.edu.pe.Saludabemente.notificaciones.shared.enums.CanalNotificacion;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DespachadorEmailServiceImpl implements DespachadorEmailService {

    private final JavaMailSender mailSender;
    private final PlantillaEngineService plantillaEngine;

    @Override
    public void despachar(String destinatario,
                          String codigoEvento,
                          Map<String, Object> variables) {

        String asunto = plantillaEngine.renderizarAsunto(codigoEvento, CanalNotificacion.EMAIL, variables);
        String cuerpo = plantillaEngine.renderizarCuerpo(codigoEvento, CanalNotificacion.EMAIL, variables);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(cuerpo, true);
            mailSender.send(message);
            log.info("Email enviado a {} (evento={})", destinatario, codigoEvento);
        } catch (Exception e) {
            log.warn("Fallo al enviar email a {} (evento={}): {}",
                    destinatario, codigoEvento, e.getMessage());
            throw new DeliveryFallidoException(
                    "No se pudo enviar email a " + destinatario + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void despacharPayloadCrudo(String destinatario, String cuerpoHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject("Reintento: notificación Saludablemente");
            helper.setText(cuerpoHtml, true);
            mailSender.send(message);
            log.info("Reintento enviado a {}", destinatario);
        } catch (Exception e) {
            log.warn("Reintento falló para {}: {}", destinatario, e.getMessage());
            throw new DeliveryFallidoException(
                    "Reintento falló para " + destinatario + ": " + e.getMessage(), e);
        }
    }
}