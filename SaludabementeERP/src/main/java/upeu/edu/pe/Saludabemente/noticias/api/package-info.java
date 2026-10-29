/**
 * Contrato público del módulo Noticias para otros módulos.
 *
 * <h3>Consumidores</h3>
 * <ul>
 *   <li><b>Dashboard</b> — últimas noticias para el panel principal.</li>
 *   <li><b>Perfil</b> — noticias relevantes al colaborador.</li>
 *   <li><b>Notificaciones</b> — referencia a una noticia publicada.</li>
 * </ul>
 *
 * <h3>Contrato expuesto</h3>
 * <ul>
 *   <li>{@code NoticiaQueryApi} — interfaz de solo lectura.</li>
 *   <li>{@code NoticiaFeedPublicDTO} — DTO resumido del feed.</li>
 * </ul>
 *
 * <h3>NO exponer</h3>
 * <ul>
 *   <li>{@code NoticiaEntity} — entidad JPA.</li>
 *   <li>{@code NoticiaRepository} — acceso a datos.</li>
 *   <li>{@code NoticiaService} — lógica interna de escritura.</li>
 *   <li>Paquetes {@code noticia.*}, {@code categoria.*}, {@code shared.*}.</li>
 * </ul>
 */
@org.springframework.modulith.NamedInterface("noticias-api")
package upeu.edu.pe.Saludabemente.noticias.api;