package upeu.edu.pe.Saludabemente.nutricional.api;

/**
 * Contrato de solo lectura del módulo Nutricional hacia Metas.
 *
 * <p>Metas lo usa para validar que una evaluación existe y obtener los
 * valores y diagnósticos clínicos necesarios para inicializar metas
 * prescritas.</p>
 */
public interface EvaluacionMetasApiService {

    /**
     * Verifica que la evaluación existe y pertenece a la persona indicada.
     *
     * @throws IllegalArgumentException si algún argumento es null
     */
    boolean existeEvaluacion(Long idEvaluacion, Long idPersona);

    /**
     * Devuelve el contexto clínico completo de la evaluación.
     *
     * @throws IllegalArgumentException si {@code idEvaluacion} es null
     * @throws upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException
     *         si la evaluación no existe
     */
    ContextoClinicoDTO obtenerContextoClinico(Long idEvaluacion);
}