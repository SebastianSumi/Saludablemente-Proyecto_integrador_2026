/**
 * Contrato público del módulo Seguridad y Usuarios.
 *
 * <p>Todo lo que otros módulos (Actividades, Teams, Reportes, etc.)
 * pueden ver y consumir vive aquí. Cualquier otra clase del módulo
 * Seguridad es interna y NO debe importarse desde fuera.</p>
 *
 * <h3>Consumidores actuales</h3>
 * <ul>
 *   <li><b>Actividades</b>: valida coordinadores y resuelve nombres
 *       para mostrar en la agenda.</li>
 * </ul>
 *
 * <h3>Regla de uso</h3>
 * Los módulos consumidores:
 * <ul>
 *   <li>Inyectan {@link upeu.edu.pe.Saludabemente.seguridad.api.SeguridadService}.</li>
 *   <li>Reciben {@link upeu.edu.pe.Saludabemente.seguridad.api.UsuarioSnapshotDto}.</li>
 *   <li><b>NO</b> acceden al CRUD de usuarios, entidades JPA ni repositorios.</li>
 * </ul>
 */
@org.springframework.modulith.NamedInterface("seguridad-api")
package upeu.edu.pe.Saludabemente.seguridad.api;