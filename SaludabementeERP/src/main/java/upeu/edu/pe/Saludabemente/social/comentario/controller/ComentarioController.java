package upeu.edu.pe.Saludabemente.social.comentario.controller;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import upeu.edu.pe.Saludabemente.social.post.dto.*;
import upeu.edu.pe.Saludabemente.social.post.service.SocialConsultaService;
import upeu.edu.pe.Saludabemente.social.comentario.service.ComentarioService;
import upeu.edu.pe.Saludabemente.social.integracion.client.SocialUsuarioClientService;
import upeu.edu.pe.Saludabemente.shared.storage.social.AdjuntoSocialDto;

@RestController @RequestMapping("/api/v1/social/posts/{post}/comentarios") @RequiredArgsConstructor @Tag(name="Social - Comentarios")
public class ComentarioController {
    private final ComentarioService comentarios;private final SocialConsultaService consulta;private final SocialUsuarioClientService usuarios;
    @GetMapping("/{id}") public ComentarioResponse detalle(@PathVariable Long post,@PathVariable Long id) {
        var u=usuarios.obtenerUsuarioActual();var c=consulta.comentario(id,u);
        if(!c.idPost().equals(post)) throw new upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException("Comentario no encontrado en la publicación");
        return c;
    }
    @GetMapping public Page<ComentarioResponse> listar(@PathVariable Long post,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {var u=usuarios.obtenerUsuarioActual();return consulta.comentarios(post,consulta.pagina(page,size,Sort.by("fechaComentario","idComentario")),u);}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public ComentarioResponse crear(@PathVariable Long post,@Valid @RequestBody CrearComentarioRequest r) {var u=usuarios.obtenerUsuarioActual();return consulta.comentario(comentarios.crear(post,r,u),u);}
    @PatchMapping("/{id}") public ComentarioResponse editar(@PathVariable Long post,@PathVariable Long id,@Valid @RequestBody EditarComentarioRequest r) {var u=usuarios.obtenerUsuarioActual();comentarios.editar(post,id,r,u);return consulta.comentario(id,u);}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void borrar(@PathVariable Long post,@PathVariable Long id) {comentarios.eliminar(post,id,usuarios.obtenerUsuarioActual());}
    @PostMapping(value="/adjuntos",consumes="multipart/form-data") @ResponseStatus(HttpStatus.CREATED) public AdjuntoSocialDto subir(@PathVariable Long post,@RequestPart("archivo") MultipartFile archivo) {return comentarios.subir(post,archivo,usuarios.obtenerUsuarioActual());}
}
