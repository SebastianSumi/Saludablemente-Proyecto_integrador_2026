/**
 * Contrato de Seguridad hacia el módulo Metas.
 *
 * <p>Subpaquete dedicado con su propio {@code @NamedInterface} para
 * aislar la evolución del contrato de otros consumidores (Actividades,
 * Noticias).</p>
 *
 * <h3>Consumidor</h3>
 * <ul>
 *   <li><b>Metas</b>: resuelve el usuario ejecutor y autoriza por rol
 *       en los endpoints REST, sin depender de Spring Security.</li>
 * </ul>
 *
 * <h3>Reglas del contrato</h3>
 * <ul>
 *   <li>Solo lectura. No modifica estado.</li>
 *   <li>No expone entidades JPA ni repositorios.</li>
 *   <li>No expone {@code password_hash} ni detalles internos.</li>
 *   <li>La resolución del usuario actual es transparente al consumidor
 *       (header hoy, JWT cuando llegue).</li>
 * </ul>
 *
 * <h3>Errores</h3>
 * <ul>
 *   <li>{@code ResourceNotFoundException} → HTTP 404 cuando no se puede
 *       resolver el usuario actual o no existe en BD.</li>
 *   <li>{@code IllegalArgumentException} → HTTP 400 cuando el código de
 *       rol es nulo o vacío.</li>
 * </ul>
 */
@org.springframework.modulith.NamedInterface("seguridad-api-metas")
package upeu.edu.pe.Saludabemente.seguridad.api.metas;