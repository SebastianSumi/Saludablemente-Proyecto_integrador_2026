package upeu.edu.pe.Saludabemente.social.seeder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Value;
import upeu.edu.pe.Saludabemente.social.post.repository.PostRepository;
import upeu.edu.pe.Saludabemente.social.post.service.PostService;
import upeu.edu.pe.Saludabemente.social.post.dto.CrearPostRequest;
import upeu.edu.pe.Saludabemente.seguridad.api.social.*;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import java.util.List;

/** Opt-in explícito: no introduce usuarios ni IDs mágicos y no corre fuera de dev. */
@Component @Profile("dev") @Order(40)
@ConditionalOnProperty(name="saludablemente.social.seed.enabled",havingValue="true")
@RequiredArgsConstructor
public class SocialDataSeeder implements CommandLineRunner {
    private final PostRepository posts;
    private final PostService servicio;
    private final SocialUsuarioApi usuarios;
    @Value("${saludablemente.social.seed.usuario-id:0}") private Long idUsuario;
    @Override public void run(String... args) {
        if(posts.count()>0) return;
        if(idUsuario==null||idUsuario<=0) throw new BusinessRuleException("Configurar social.seed.usuario-id de una cuenta activa");
        var u=usuarios.obtenerPorId(idUsuario);
        servicio.crear(new CrearPostRequest("Bienvenido a Social","Espacio institucional para compartir bienestar #salud",null,List.of()),
                new SocialUsuarioActualDto(u.idUsuario(),u.username(),u.email(),u.roles(),u.roles().contains("Administrador")));
    }
}
