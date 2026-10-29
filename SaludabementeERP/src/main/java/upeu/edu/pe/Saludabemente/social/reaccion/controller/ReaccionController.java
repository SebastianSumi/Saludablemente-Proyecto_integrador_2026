package upeu.edu.pe.Saludabemente.social.reaccion.controller;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import upeu.edu.pe.Saludabemente.social.post.dto.*;
import upeu.edu.pe.Saludabemente.social.post.service.SocialConsultaService;
import upeu.edu.pe.Saludabemente.social.reaccion.service.InteraccionService;
import upeu.edu.pe.Saludabemente.social.integracion.client.SocialUsuarioClientService;
import upeu.edu.pe.Saludabemente.social.api.dto.TipoReaccion;

@RestController
@RequestMapping("/api/v1/social")
@RequiredArgsConstructor
@Tag(name="Social - Reacciones")

public class ReaccionController {

    private final InteraccionService interacciones;
    private final SocialConsultaService consulta;
    private final SocialUsuarioClientService usuarios;

    @PostMapping("/posts/{id}/reacciones")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void post(@PathVariable Long id,@Valid @RequestBody ReaccionarRequest r) {
        interacciones.reaccionar(id,r.tipo(),usuarios.obtenerUsuarioActual());
    }

    @DeleteMapping("/posts/{id}/reacciones")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitarPost(@PathVariable Long id) {
        interacciones.reaccionar(id,null,usuarios.obtenerUsuarioActual());
    }

    @PostMapping("/comentarios/{id}/reacciones")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void comentario(@PathVariable Long id,@Valid @RequestBody ReaccionarRequest r) {
        interacciones.reaccionarComentario(id,r.tipo(),usuarios.obtenerUsuarioActual());
    }

    @DeleteMapping("/comentarios/{id}/reacciones")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitarComentario(@PathVariable Long id) {
        interacciones.reaccionarComentario(id,null,usuarios.obtenerUsuarioActual());
    }
    @GetMapping("/posts/{id}/reacciones")
    public ReaccionResumenResponse resumenPost(@PathVariable Long id) {
        return consulta.reaccionPost(id,usuarios.obtenerUsuarioActual());
    }
    @GetMapping("/comentarios/{id}/reacciones")
    public ReaccionResumenResponse resumenComentario(@PathVariable Long id) {
        return consulta.reaccionComentario(id,usuarios.obtenerUsuarioActual());
    }

    @GetMapping("/posts/{id}/reacciones/usuarios")
    public Page<ReaccionDetalleResponse> usuariosPost(@PathVariable Long id,@RequestParam(required=false) TipoReaccion tipo,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        usuarios.obtenerUsuarioActual();
        return consulta.quienReacciono(id,false,tipo,consulta.pagina(page,size,Sort.by(Sort.Direction.DESC,"fechaReaccion","idUsuario")));
    }

    @GetMapping("/comentarios/{id}/reacciones/usuarios")
    public Page<ReaccionDetalleResponse> usuariosComentario(@PathVariable Long id,@RequestParam(required=false) TipoReaccion tipo,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        usuarios.obtenerUsuarioActual();
        return consulta.quienReacciono(id,true,tipo,consulta.pagina(page,size,Sort.by(Sort.Direction.DESC,"fechaReaccion","idUsuario")));
    }
}
