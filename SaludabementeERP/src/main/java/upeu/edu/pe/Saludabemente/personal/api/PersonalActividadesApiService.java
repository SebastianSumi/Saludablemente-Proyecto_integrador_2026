package upeu.edu.pe.Saludabemente.personal.api;

import java.util.List;

/**
 * API publica del modulo Personal, expuesta exclusivamente al modulo
 * Actividades para el subdominio de inscripcion.
 *
 * <p>Contrato de solo lectura. Actividades nunca crea, modifica ni elimina
 * personas.</p>
 *
 * <h3>NO exponer</h3>
 * <ul>
 *   <li>{@code Persona} — entidad JPA.</li>
 *   <li>{@code PersonaRepository} — acceso a datos (regla S3).</li>
 *   <li>{@code PersonaService} — servicio interno.</li>
 * </ul>
 */
public interface PersonalActividadesApiService {

    /**
     * IDs de todas las personas activas.
     * Nunca devuelve {@code null}. Si no hay ninguna, devuelve lista vacia.
     * Sin duplicados.
     */
    List<Long> listarPersonasActivasIds();

    /**
     * Snapshot completo de personas activas, ordenadas alfabeticamente
     * por apellido paterno, apellido materno y nombres.
     * Nunca devuelve {@code null}.
     */
    List<PersonaActividadesDTO> listarPersonasActivas();

    /**
     * Snapshot de las personas cuyos IDs se solicitan.
     * <ul>
     *   <li>Si {@code ids} es {@code null} o vacio: devuelve lista vacia.</li>
     *   <li>Los IDs que no existan se omiten (no lanza excepcion).</li>
     * </ul>
     * Nunca devuelve {@code null}. Sin duplicados.
     */
    List<PersonaActividadesDTO> listarPersonasPorIds(List<Long> ids);

    /**
     * Valida que todas las personas existan y esten activas.
     * <ul>
     *   <li>Si {@code ids} es {@code null} o vacio: no hace nada.</li>
     *   <li>Si algun ID no existe: lanza {@code BusinessRuleException}.</li>
     *   <li>Si algun ID esta inactivo: lanza {@code BusinessRuleException}
     *       con la lista de los inactivos.</li>
     *   <li>Si todo esta OK: retorna sin excepcion.</li>
     * </ul>
     */
    void validarPersonas(List<Long> ids);
}