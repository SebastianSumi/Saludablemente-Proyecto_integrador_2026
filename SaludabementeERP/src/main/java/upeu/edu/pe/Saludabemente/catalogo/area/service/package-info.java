/**
 * API pública del módulo Catálogo para consulta de áreas de trabajo.
 *
 * <h3>Consumidores</h3>
 * <ul>
 *   <li><b>Personal</b>: {@code buscarOFallar(id)} para resolver el
 *       {@code areaId} de un {@code PersonaRequest} antes de guardar.</li>
 * </ul>
 *
 * <h3>Contrato expuesto</h3>
 * <ul>
 *   <li>{@code buscarOFallar(Long)} → {@code AreaTrabajo} (entidad) —
 *       uso interno entre módulos del proyecto, no expuesta al frontend.</li>
 *   <li>{@code listarResumen()} → {@code List<AreaResumen>}.</li>
 * </ul>
 *
 * <h3>NO exponer</h3>
 * <ul>
 *   <li>{@code AreaRepository} — acceso a datos (regla S3).</li>
 *   <li>Métodos de escritura — solo los usa {@code AreaController}.</li>
 * </ul>
 */
@org.springframework.modulith.NamedInterface("area-service")
package upeu.edu.pe.Saludabemente.catalogo.area.service;