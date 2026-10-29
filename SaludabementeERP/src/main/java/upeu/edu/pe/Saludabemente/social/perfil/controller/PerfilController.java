package upeu.edu.pe.Saludabemente.social.perfil.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;
import io.swagger.v3.oas.annotations.tags.Tag;
import upeu.edu.pe.Saludabemente.social.post.dto.*;
import upeu.edu.pe.Saludabemente.social.post.service.SocialConsultaService;
import upeu.edu.pe.Saludabemente.social.integracion.client.SocialUsuarioClientService;

@RestController @RequestMapping("/api/v1/social") @RequiredArgsConstructor @Tag(name="Social - Perfil y Mi Contenido")
public class PerfilController {
    private final SocialConsultaService consulta;private final SocialUsuarioClientService usuarios;
    @GetMapping("/usuarios/{id}/actividad") public PerfilUsuarioResponse perfil(@PathVariable Long id,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {var u=usuarios.obtenerUsuarioActual();return consulta.perfil(id,consulta.pagina(page,size),consulta.pagina(page,size,Sort.by(Sort.Direction.DESC,"fechaComentario","idComentario")),u);}
    @GetMapping("/mi-contenido") public MiContenidoResponse contenido(@RequestParam(defaultValue="PUBLICACIONES") String tab,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {var u=usuarios.obtenerUsuarioActual();return consulta.miContenido(tab,consulta.pagina(page,size),u);}
}
