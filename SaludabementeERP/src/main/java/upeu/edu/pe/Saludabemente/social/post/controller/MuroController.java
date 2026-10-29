package upeu.edu.pe.Saludabemente.social.post.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import upeu.edu.pe.Saludabemente.social.post.dto.*;
import upeu.edu.pe.Saludabemente.social.post.service.SocialConsultaService;
import upeu.edu.pe.Saludabemente.social.integracion.client.SocialUsuarioClientService;
import upeu.edu.pe.Saludabemente.social.api.dto.ScopeFijado;
import upeu.edu.pe.Saludabemente.personal.api.social.*;

@RestController @RequestMapping("/api/v1/social") @RequiredArgsConstructor @Tag(name="Social - Muro")
public class MuroController {
    private final SocialConsultaService consulta;private final SocialUsuarioClientService usuarios;private final SocialEquipoApi equipos;
    @GetMapping("/muro") public MuroResponse muro(@RequestParam(defaultValue="PARA_TI") String tab,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {var u=usuarios.obtenerUsuarioActual();return consulta.muro(tab,ScopeFijado.GLOBAL,null,null,consulta.pagina(page,size),u);}
    @GetMapping("/muro/equipos/{id}") public MuroResponse equipo(@PathVariable Long id,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {var u=usuarios.obtenerUsuarioActual();equipos.obtenerPorId(id);return consulta.muro("RECIENTES",ScopeFijado.EQUIPO,id,null,consulta.pagina(page,size),u);}
    @GetMapping("/equipos/{id}/perfil") public SocialEquipoPerfilDto perfilEquipo(@PathVariable Long id) {usuarios.obtenerUsuarioActual();return equipos.obtenerPerfil(id);}
    @GetMapping("/muro/actividades/{id}") public MuroResponse actividad(@PathVariable Long id,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {var u=usuarios.obtenerUsuarioActual();return consulta.muro("RECIENTES",ScopeFijado.ACTIVIDAD,id,null,consulta.pagina(page,size),u);}
    @GetMapping("/hashtags/{tag}/muro") public MuroResponse tag(@PathVariable String tag,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {var u=usuarios.obtenerUsuarioActual();return consulta.muro("RECIENTES",ScopeFijado.GLOBAL,null,tag,consulta.pagina(page,size),u);}
}
