package upeu.edu.pe.Saludabemente.social.post.controller;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import upeu.edu.pe.Saludabemente.social.post.dto.*;
import upeu.edu.pe.Saludabemente.social.post.service.*;
import upeu.edu.pe.Saludabemente.social.reaccion.service.InteraccionService;
import upeu.edu.pe.Saludabemente.social.integracion.client.SocialUsuarioClientService;
import upeu.edu.pe.Saludabemente.social.api.dto.ScopeFijado;

@RestController @RequestMapping("/api/v1/social/posts") @RequiredArgsConstructor @Tag(name="Social - Posts")
public class PostController {
    private final PostService posts;private final SocialConsultaService consulta;private final InteraccionService interacciones;
    private final FijadoService fijados;private final SocialUsuarioClientService usuarios;
    @GetMapping("/{id}") public PostDetalleResponse detalle(@PathVariable Long id) {var u=usuarios.obtenerUsuarioActual();interacciones.registrarVista(id,u);return consulta.detalle(id,u);}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public PostDetalleResponse crear(@Valid @RequestBody CrearPostRequest r) {var u=usuarios.obtenerUsuarioActual();return consulta.detalle(posts.crear(r,u),u);}
    @PatchMapping("/{id}") public PostDetalleResponse editar(@PathVariable Long id,@Valid @RequestBody EditarPostRequest r) {var u=usuarios.obtenerUsuarioActual();posts.editar(id,r,u);return consulta.detalle(id,u);}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void borrar(@PathVariable Long id) {posts.eliminar(id,usuarios.obtenerUsuarioActual());}
    @PostMapping("/{id}/share") @ResponseStatus(HttpStatus.CREATED) public ShareResponse compartir(@PathVariable Long id,@Valid @RequestBody CompartirRequest r) {return interacciones.compartir(id,r,usuarios.obtenerUsuarioActual());}
    @PostMapping("/{id}/guardar") @ResponseStatus(HttpStatus.NO_CONTENT) public void guardar(@PathVariable Long id) {interacciones.guardar(id,true,usuarios.obtenerUsuarioActual());}
    @DeleteMapping("/{id}/guardar") @ResponseStatus(HttpStatus.NO_CONTENT) public void quitarGuardado(@PathVariable Long id) {interacciones.guardar(id,false,usuarios.obtenerUsuarioActual());}
    @PostMapping("/{id}/fijar") @ResponseStatus(HttpStatus.NO_CONTENT) public void fijar(@PathVariable Long id,@Valid @RequestBody FijarRequest r) {fijados.fijar(id,r,usuarios.obtenerUsuarioActual());}
    @DeleteMapping("/{id}/fijar") @ResponseStatus(HttpStatus.NO_CONTENT) public void desfijar(@PathVariable Long id,@RequestParam ScopeFijado scopeTipo,@RequestParam(required=false) Long scopeId) {fijados.quitar(id,scopeTipo,scopeId,usuarios.obtenerUsuarioActual());}
}
