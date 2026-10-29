/**
 * DTOs públicos del módulo Noticias.
 *
 * <h3>Consumidores</h3>
 * <ul>
 *   <li><b>Dashboard</b>: {@code NoticiaFeedPublicDTO} para el panel principal.</li>
 *   <li><b>Perfil</b>: {@code NoticiaFeedPublicDTO} para noticias del colaborador.</li>
 *   <li><b>Notificaciones</b>: {@code NoticiaFeedPublicDTO} al referenciar una noticia.</li>
 * </ul>
 *
 * <h3>NO exponer</h3>
 * <ul>
 *   <li>{@code NoticiaEntity} — entidad JPA.</li>
 *   <li>{@code NoticiaRepository} — acceso a datos.</li>
 *   <li>DTOs internos del submódulo noticia.</li>
 * </ul>
 */
@org.springframework.modulith.NamedInterface("noticias-dto")
package upeu.edu.pe.Saludabemente.noticias.api.dto;