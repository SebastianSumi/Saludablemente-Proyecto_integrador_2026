package upeu.edu.pe.socialcontracts;
import org.junit.jupiter.api.Test;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.seeder.SocialPlantillasDataSeeder;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.entity.PlantillaMensaje;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.repository.PlantillaMensajeRepository;
import upeu.edu.pe.Saludabemente.social.seeder.SocialDataSeeder;
import upeu.edu.pe.Saludabemente.social.post.repository.PostRepository;
import upeu.edu.pe.Saludabemente.social.post.service.PostService;
import upeu.edu.pe.Saludabemente.seguridad.api.social.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SocialSeedersTest {
    @Test void plantillasSeCreanUnaVezSinPisarEditadas() {
        var repo=mock(PlantillaMensajeRepository.class);Map<String,PlantillaMensaje> guardadas=new HashMap<>();
        when(repo.findByCodigoEvento(anyString())).thenAnswer(i->Optional.ofNullable(guardadas.get(i.getArgument(0))));
        when(repo.save(any())).thenAnswer(i->{PlantillaMensaje p=i.getArgument(0);guardadas.put(p.getCodigoEvento(),p);return p;});
        var seeder=new SocialPlantillasDataSeeder(repo);seeder.run();guardadas.get("SOCIAL_MENCION").setAsunto("Editada");seeder.run();
        assertEquals(5,guardadas.size());assertEquals("Editada",guardadas.get("SOCIAL_MENCION").getAsunto());verify(repo,times(5)).save(any());
    }
    @Test void seederSocialNecesitaCuentaExplicitaYOmitirSiHayContenido() {
        var repo=mock(PostRepository.class);var servicio=mock(PostService.class);var usuarios=mock(SocialUsuarioApi.class);var seeder=new SocialDataSeeder(repo,servicio,usuarios);
        org.springframework.test.util.ReflectionTestUtils.setField(seeder,"idUsuario",101L);
        when(usuarios.obtenerPorId(101L)).thenReturn(new SocialUsuarioSnapshotDto(101L,"ana",null,"Ana",null,List.of("Colaborador"),"Colaborador",null,true));
        seeder.run();when(repo.count()).thenReturn(1L);seeder.run();verify(servicio,times(1)).crear(any(),argThat(u->u.idUsuario().equals(101L)));
        assertTrue(SocialDataSeeder.class.isAnnotationPresent(org.springframework.context.annotation.Profile.class));
    }
}
