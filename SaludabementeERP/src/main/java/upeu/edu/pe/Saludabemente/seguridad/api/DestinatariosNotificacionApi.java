package upeu.edu.pe.Saludabemente.seguridad.api;

import java.util.Set;

/**
 * Resolución de destinatarios para notificaciones originadas en otros módulos.
 *
 * <p>Devuelve <b>idPersona</b> (no idUsuario), listos para
 * {@code NotificacionApi#enviar}. El resultado es un conjunto: si una misma
 * persona es dueña, evaluadora y administradora, aparece una sola vez.</p>
 */
public interface DestinatariosNotificacionApi {

    /**
     * Dueño de la alerta + usuarios activos con roles de gestión
     * (Administrador, Coordinador, Evaluador) + evaluador que atiende (si se indica).
     *
     * <p>Cada grupo es opcional: el dueño sin cuenta y el evaluador ausente
     * simplemente no aportan destinatarios, sin lanzar excepción.</p>
     *
     * @param idPersonaDuena      persona a la que pertenece la alerta (puede no tener cuenta)
     * @param idUsuarioEvaluador  usuario que atiende/anula (puede ser null)
     * @return conjunto de idPersona destinatarios (nunca null)
     */
    Set<Long> resolverDestinatariosAlerta(Long idPersonaDuena, Long idUsuarioEvaluador);
}
