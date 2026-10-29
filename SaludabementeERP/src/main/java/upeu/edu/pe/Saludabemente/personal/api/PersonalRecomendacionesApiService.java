package upeu.edu.pe.Saludabemente.personal.api;

import java.util.Optional;

/**
 * API publica del modulo Personal, expuesta exclusivamente al modulo
 * Recomendaciones IA.
 *
 * <p>Contrato de solo lectura. Ningun metodo modifica estado, expone
 * entidades ni lanza excepcion por ausencia de datos.</p>
 *
 * <h3>NO exponer</h3>
 * <ul>
 *   <li>{@code Persona} — entidad JPA.</li>
 *   <li>{@code PersonaRepository} — acceso a datos (regla S3).</li>
 *   <li>{@code PersonaService} — servicio interno.</li>
 * </ul>
 */
public interface PersonalRecomendacionesApiService {

    /**
     * Devuelve los datos demograficos de una persona (activa o inactiva).
     *
     * <p>Si {@code idPersona} es {@code null} o no existe, devuelve
     * {@link Optional#empty()}. No lanza excepcion por ausencia de datos.</p>
     *
     * <p>El campo {@code activo} se devuelve tal cual. El consumidor
     * decide si genera recomendaciones para personas inactivas.</p>
     */
    Optional<PersonaRecomendacionesDTO> obtenerDatosDemograficos(Long idPersona);
}