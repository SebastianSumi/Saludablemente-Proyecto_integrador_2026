/**
 * Contrato público del módulo Catálogo para áreas de trabajo.
 *
 * <h3>Consumidores</h3>
 * <ul>
 *   <li><b>Personal</b>: usa {@code AreaResumen} embebido en
 *       {@code PersonaResponse}, y {@code AreaResponse} para poblar los
 *       selects del frontend.</li>
 * </ul>
 *
 * <h3>Contrato expuesto</h3>
 * <ul>
 *   <li>{@code AreaResponse} — datos completos del área.</li>
 *   <li>{@code AreaResumen} — vista reducida (id, nombre).</li>
 * </ul>
 *
 * <h3>NO exponer</h3>
 * <ul>
 *   <li>{@code AreaTrabajo} — entidad JPA.</li>
 *   <li>{@code AreaRepository} — acceso a datos (regla S3).</li>
 *   <li>{@code AreaMapper}, {@code AreaServiceImpl} — detalles internos.</li>
 * </ul>
 */
@org.springframework.modulith.NamedInterface("area-dto")
package upeu.edu.pe.Saludabemente.catalogo.area.dto;