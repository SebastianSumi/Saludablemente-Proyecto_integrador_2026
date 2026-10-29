package upeu.edu.pe.Saludabemente.social.moderacion.controller;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import upeu.edu.pe.Saludabemente.social.post.dto.*;
import upeu.edu.pe.Saludabemente.social.post.service.*;
import upeu.edu.pe.Saludabemente.social.moderacion.service.ModeracionService;
import upeu.edu.pe.Saludabemente.social.integracion.client.SocialUsuarioClientService;
import upeu.edu.pe.Saludabemente.social.api.dto.EstadoReporte;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;

@RestController @RequestMapping("/api/v1/social") @RequiredArgsConstructor @Tag(name="Social - Moderación (Admin)")
public class ModeracionController {
    private final ModeracionService moderacion;private final SocialConsultaService consulta;private final SocialAccesoService acceso;private final SocialUsuarioClientService usuarios;
    @PostMapping("/posts/{id}/reportar") @ResponseStatus(HttpStatus.CREATED) public ReporteResponse post(@PathVariable Long id,@Valid @RequestBody ReportarRequest r) {return moderacion.reportar(id,null,r,usuarios.obtenerUsuarioActual());}
    @PostMapping("/posts/{post}/comentarios/{id}/reportar") @ResponseStatus(HttpStatus.CREATED) public ReporteResponse comentario(@PathVariable Long post,@PathVariable Long id,@Valid @RequestBody ReportarRequest r) {var u=usuarios.obtenerUsuarioActual();if(!acceso.comentarioVisible(id).getIdPost().equals(post)) throw new BusinessRuleException("Comentario de otro post");return moderacion.reportar(null,id,r,u);}
    @GetMapping("/admin/reportes") public Page<ReporteResponse> reportes(@RequestParam(defaultValue="PENDIENTE") EstadoReporte estado,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {return moderacion.listar(estado,consulta.pagina(page,size,Sort.by("fechaReporte","idPostReporte")),usuarios.obtenerUsuarioActual());}
    @PatchMapping("/admin/reportes/{id}") public ReporteResponse resolver(@PathVariable Long id,@Valid @RequestBody ResolverReporteRequest r) {return moderacion.resolver(id,r,usuarios.obtenerUsuarioActual());}
    @PatchMapping("/admin/posts/{id}/ocultar") @ResponseStatus(HttpStatus.NO_CONTENT) public void ocultarPost(@PathVariable Long id) {moderacion.ocultarPost(id,false,usuarios.obtenerUsuarioActual());}
    @PatchMapping("/admin/posts/{id}/restaurar") @ResponseStatus(HttpStatus.NO_CONTENT) public void restaurarPost(@PathVariable Long id) {moderacion.ocultarPost(id,true,usuarios.obtenerUsuarioActual());}
    @PatchMapping("/admin/comentarios/{id}/ocultar") @ResponseStatus(HttpStatus.NO_CONTENT) public void ocultarComentario(@PathVariable Long id) {moderacion.ocultarComentario(id,false,usuarios.obtenerUsuarioActual());}
    @PatchMapping("/admin/comentarios/{id}/restaurar") @ResponseStatus(HttpStatus.NO_CONTENT) public void restaurarComentario(@PathVariable Long id) {moderacion.ocultarComentario(id,true,usuarios.obtenerUsuarioActual());}
}
