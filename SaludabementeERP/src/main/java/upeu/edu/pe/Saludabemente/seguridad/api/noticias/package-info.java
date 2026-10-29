/**
 * Contrato de Seguridad hacia el módulo Noticias.
 *
 * <p>Subpaquete dedicado (no comparte el espacio con el contrato de
 * Actividades) para aislar la evolución de ambos consumidores. Tiene su
 * propio {@code @NamedInterface}, así Modulith trata esta frontera como
 * una unidad independiente.</p>
 *
 * <h3>Consumidor</h3>
 * <ul>
 *   <li><b>Noticias</b>: valida autores de noticias, resuelve nombres
 *       para mostrar en el panel de administración y obtiene el vínculo
 *       lógico con Persona.</li>
 * </ul>
 *
 * <h3>Reglas del contrato</h3>
 * <ul>
 *   <li>Solo lectura. Nunca expone métodos de escritura.</li>
 *   <li>Lanza excepciones tipadas cuando el dato no existe —
 *       <b>no</b> devuelve {@code Optional.empty()} ni {@code null}.</li>
 *   <li>No expone entidades JPA ni repositorios.</li>
 *   <li>No expone {@code password_hash} ni detalles internos.</li>
 * </ul>
 *
 * <h3>Errores</h3>
 * <ul>
 *   <li>{@code ResourceNotFoundException} → HTTP 404 cuando el usuario
 *       o el vínculo no existen.</li>
 *   <li>{@code BusinessRuleException} → HTTP 409 cuando el usuario
 *       existe pero no cumple la regla de negocio (ej. inactivo).</li>
 * </ul>
 */
@org.springframework.modulith.NamedInterface("seguridad-api-noticias")
package upeu.edu.pe.Saludabemente.seguridad.api.noticias;