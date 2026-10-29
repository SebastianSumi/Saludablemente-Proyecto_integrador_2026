package upeu.edu.pe.Saludabemente.notificaciones.notificacion.seeder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.notificaciones.api.social.TipoAvisoSocial;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.entity.PlantillaMensaje;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.repository.PlantillaMensajeRepository;
import upeu.edu.pe.Saludabemente.notificaciones.shared.enums.CanalNotificacion;
import java.util.Map;

/** Mismo patrón idempotente de plantillas existente; antes de los seeders de contenido. */
@Component @Order(-10) @RequiredArgsConstructor
public class SocialPlantillasDataSeeder implements CommandLineRunner {
    private final PlantillaMensajeRepository plantillas;
    @Override @Transactional public void run(String... args) {
        Map<TipoAvisoSocial,String> asuntos=Map.of(
            TipoAvisoSocial.SOCIAL_COMENTARIO_POST,"Nuevo comentario",
            TipoAvisoSocial.SOCIAL_RESPUESTA_COMENTARIO,"Nueva respuesta",
            TipoAvisoSocial.SOCIAL_MENCION,"Te mencionaron",
            TipoAvisoSocial.SOCIAL_REACCION_POST,"Nueva reacción",
            TipoAvisoSocial.SOCIAL_NUEVO_POST_EQUIPO,"Nueva publicación de tu equipo");
        Map<TipoAvisoSocial,String> cuerpos=Map.of(
            TipoAvisoSocial.SOCIAL_COMENTARIO_POST,"<p>Tu publicación {{idPost}} recibió un comentario.</p>",
            TipoAvisoSocial.SOCIAL_RESPUESTA_COMENTARIO,"<p>Tu comentario recibió una respuesta en la publicación {{idPost}}.</p>",
            TipoAvisoSocial.SOCIAL_MENCION,"<p>Te mencionaron en la publicación {{idPost}}.</p>",
            TipoAvisoSocial.SOCIAL_REACCION_POST,"<p>Tu publicación {{idPost}} recibió una reacción.</p>",
            TipoAvisoSocial.SOCIAL_NUEVO_POST_EQUIPO,"<p>El equipo {{idEquipo}} publicó contenido nuevo: {{idPost}}.</p>");
        for(var tipo:TipoAvisoSocial.values()) if(plantillas.findByCodigoEvento(tipo.name()).isEmpty()) {
            var p=new PlantillaMensaje();p.setCodigoEvento(tipo.name());p.setCanal(CanalNotificacion.IN_APP);
            p.setAsunto(asuntos.get(tipo));p.setCuerpoHtml(cuerpos.get(tipo));p.setActivo(true);plantillas.save(p);
        }
    }
}
