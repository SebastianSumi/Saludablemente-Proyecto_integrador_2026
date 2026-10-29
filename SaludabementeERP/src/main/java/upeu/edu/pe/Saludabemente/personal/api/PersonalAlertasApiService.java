package upeu.edu.pe.Saludabemente.personal.api;

/**
 * API publica del modulo Personal, expuesta exclusivamente al modulo
 * Alertas Clinicas.
 *
 * <p>Contrato de solo lectura. Alertas nunca crea, modifica ni elimina
 * personas.</p>
 *
 * <h3>NO exponer</h3>
 * <ul>
 *   <li>{@code Persona} — entidad JPA.</li>
 *   <li>{@code PersonaRepository} — acceso a datos (regla S3).</li>
 *   <li>{@code PersonaService} — servicio interno.</li>
 * </ul>
 */
public interface PersonalAlertasApiService {

    /**
     * Devuelve el contexto demografico de una persona para el motor de
     * alertas clinicas.
     *
     * <ul>
     *   <li>Si {@code idPersona} es {@code null}: lanza
     *       {@code IllegalArgumentException}.</li>
     *   <li>Si la persona no existe: lanza {@code ResourceNotFoundException}.</li>
     *   <li>Si la persona esta inactiva: devuelve el contexto con normalidad
     *       (permite evaluar historicos).</li>
     * </ul>
     */
    PacienteContextoDTO obtenerContexto(Long idPersona);
}