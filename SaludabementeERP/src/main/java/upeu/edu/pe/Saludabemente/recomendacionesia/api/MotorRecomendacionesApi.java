package upeu.edu.pe.Saludabemente.recomendacionesia.api;

/**
 * API pública del módulo Recomendaciones IA.
 *
 * Expone un único método para que el módulo Nutricional (Sebastián)
 * dispare la generación de recomendaciones cuando una evaluación pasa
 * a estado COMPLETA.
 *
 * El método retorna inmediatamente. La generación real ocurre en segundo
 * plano mediante el outbox + scheduler del módulo.
 *
 * Es idempotente: si ya existe un pendiente activo o una recomendación
 * vigente para el par (idPersona, idEvaluacion), la llamada se ignora.
 */
public interface MotorRecomendacionesApi {

    void dispararGeneracion(Long idEvaluacion, Long idPersona);
}