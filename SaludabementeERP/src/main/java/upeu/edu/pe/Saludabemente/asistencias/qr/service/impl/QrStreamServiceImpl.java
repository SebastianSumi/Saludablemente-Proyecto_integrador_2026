package upeu.edu.pe.Saludabemente.asistencias.qr.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import upeu.edu.pe.Saludabemente.asistencias.qr.dto.response.QrActividadResponse;
import upeu.edu.pe.Saludabemente.asistencias.qr.service.QrStreamService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class QrStreamServiceImpl implements QrStreamService {

    private final Map<Long, List<SseEmitter>> emisores = new ConcurrentHashMap<>();

    @Override
    public SseEmitter suscribir(Long idActividad) {
        SseEmitter emitter = new SseEmitter(0L); // sin timeout

        emisores.computeIfAbsent(idActividad, k -> new CopyOnWriteArrayList<>())
                .add(emitter);

        emitter.onCompletion(() -> remover(idActividad, emitter));
        emitter.onTimeout(() -> remover(idActividad, emitter));
        emitter.onError(e -> remover(idActividad, emitter));

        log.info("SSE suscrito a actividad={}", idActividad);
        return emitter;
    }

    @Override
    public void emitir(Long idActividad, QrActividadResponse qr) {
        List<SseEmitter> lista = emisores.get(idActividad);
        if (lista == null || lista.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : lista) {
            try {
                emitter.send(SseEmitter.event()
                        .name("qr")
                        .data(qr));
            } catch (IOException e) {
                log.warn("Error emitiendo SSE actividad={}: {}", idActividad, e.getMessage());
                remover(idActividad, emitter);
            }
        }
    }

    @Override
    public void cerrarTodos(Long idActividad) {
        List<SseEmitter> lista = emisores.remove(idActividad);
        if (lista != null) {
            lista.forEach(SseEmitter::complete);
        }
    }

    private void remover(Long idActividad, SseEmitter emitter) {
        List<SseEmitter> lista = emisores.get(idActividad);
        if (lista != null) {
            lista.remove(emitter);
        }
    }
}