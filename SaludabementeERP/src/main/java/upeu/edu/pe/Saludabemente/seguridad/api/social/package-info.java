/**
 * Contrato de Seguridad hacia el módulo Social.
 *
 * <p>Subpaquete dedicado con su propio {@code @NamedInterface} para
 * aislar la evolución del contrato de otros consumidores (Actividades,
 * Noticias, Metas).</p>
 *
 * <h3>Consumidor</h3>
 * <ul>
 *   <li><b>Social</b>: resuelve autores de posts, valida usuarios en
 *       menciones y enriquece el feed con datos de identidad.</li>
 * </ul>
 *
 * <h3>Reglas del contrato</h3>
 * <ul>
 *   <li>Solo lectura. No modifica estado.</li>
 *   <li>No expone entidades JPA ni repositorios.</li>
 *   <li>El identificador de menciones es el {@code username} corto
 *       (ej. {@code ana.lazaro}), no el email.</li>
 *   <li>{@code fotoUrl} puede ser {@code null} hasta que exista el flujo
 *       de subida de imágenes. El consumidor debe mostrar iniciales.</li>
 * </ul>
 *
 * <h3>Errores</h3>
 * <ul>
 *   <li>{@code ResourceNotFoundException} → HTTP 404 cuando el usuario
 *       o la identidad no existen.</li>
 * </ul>
 */
@org.springframework.modulith.NamedInterface("seguridad-api-social")
package upeu.edu.pe.Saludabemente.seguridad.api.social;