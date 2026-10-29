package upeu.edu.pe.Saludabemente.recomendacionesia.core.service;

import upeu.edu.pe.Saludabemente.recomendacionesia.core.entity.RecomendacionPendiente;

/**
 * Orquesta el procesamiento de un pendiente del outbox.
 * Se invoca desde el OutboxScheduler.
 */
public interface OrquestadorRecomendacionService {

    /**
     * Procesa un pendiente: arma el snapshot, llama a la IA,
     * valida la respuesta y delega la consolidación.
     *
     * Si algo falla, incrementa el contador de intentos y guarda el error.
     * No lanza excepción al scheduler para que este pueda continuar con
     * el siguiente pendiente.
     */
    void procesarPendiente(RecomendacionPendiente pendiente);
}