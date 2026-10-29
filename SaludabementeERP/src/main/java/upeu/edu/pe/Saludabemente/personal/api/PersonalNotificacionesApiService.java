package upeu.edu.pe.Saludabemente.personal.api;

/**
 * API publica del modulo Personal, expuesta exclusivamente al modulo
 * Notificaciones.
 *
 * <h3>Contrato expuesto</h3>
 * <ul>
 *   <li>{@code existeYEstaActiva(Long)} — validacion booleana.</li>
 *   <li>{@code obtenerDatosBasicos(Long)} — datos minimos para personalizacion.</li>
 * </ul>
 *
 * <h3>NO exponer</h3>
 * <ul>
 *   <li>{@code Persona} — entidad JPA.</li>
 *   <li>{@code PersonaRepository} — acceso a datos (regla S3).</li>
 *   <li>{@code PersonaService} — servicio interno.</li>
 * </ul>
 */
public interface PersonalNotificacionesApiService {

    /**
     * Devuelve {@code true} solo si la persona existe y esta activa.
     * En cualquier otro caso (no existe, inactiva, id null) devuelve
     * {@code false}. Nunca lanza excepcion.
     */
    boolean existeYEstaActiva(Long idPersona);

    /**
     * Devuelve los datos basicos de una persona activa.
     *
     * @throws IllegalArgumentException si {@code idPersona} es {@code null}.
     * @throws upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException
     *         si la persona no existe o esta inactiva.
     */
    DatosBasicosPersonaDTO obtenerDatosBasicos(Long idPersona);
}