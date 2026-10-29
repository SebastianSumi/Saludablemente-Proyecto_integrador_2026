package upeu.edu.pe.Saludabemente.asistencias.qr.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import upeu.edu.pe.Saludabemente.asistencias.qr.dto.response.QrActividadResponse;

public interface QrStreamService {

    SseEmitter suscribir(Long idActividad);

    void emitir(Long idActividad, QrActividadResponse qr);

    void cerrarTodos(Long idActividad);
}