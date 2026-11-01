package upeu.edu.pe.Saludabemente.social.perfil.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import upeu.edu.pe.Saludabemente.social.integracion.client.SocialUsuarioClientService;
import upeu.edu.pe.Saludabemente.social.perfil.dto.EditarPresentacionRequest;
import upeu.edu.pe.Saludabemente.social.perfil.dto.PerfilCabeceraResponse;
import upeu.edu.pe.Saludabemente.social.perfil.service.PerfilSocialService;
import upeu.edu.pe.Saludabemente.social.post.dto.AutorResponse;
import upeu.edu.pe.Saludabemente.social.post.service.SocialConsultaService;

@RestController
@RequestMapping("/api/v1/social")
@RequiredArgsConstructor
@Tag(name = "Social - Perfil visual")
public class PerfilVisualController {

    private final PerfilSocialService perfil;
    private final SocialConsultaService consulta;
    private final SocialUsuarioClientService usuarios;

    @GetMapping("/usuarios/{id}/perfil")
    public PerfilCabeceraResponse cabecera(@PathVariable Long id) {
        return perfil.cabecera(id, usuarios.obtenerUsuarioActual());
    }

    @GetMapping("/mi-perfil")
    public PerfilCabeceraResponse propio() {
        var actor = usuarios.obtenerUsuarioActual();
        return perfil.cabecera(actor.idUsuario(), actor);
    }

    @GetMapping("/usuarios/{id}/companeros")
    public Page<AutorResponse> companeros(@PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        usuarios.obtenerUsuarioActual();
        return perfil.companeros(id, consulta.pagina(page, size));
    }

    @PatchMapping("/mi-perfil")
    public PerfilCabeceraResponse editar(@Valid @RequestBody EditarPresentacionRequest request) {
        return perfil.editar(request.presentacion(), usuarios.obtenerUsuarioActual());
    }

    @PostMapping(value = "/mi-perfil/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PerfilCabeceraResponse foto(@RequestPart("archivo") MultipartFile archivo) {
        return perfil.foto(archivo, false, usuarios.obtenerUsuarioActual());
    }

    @PostMapping(value = "/mi-perfil/portada", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PerfilCabeceraResponse portada(@RequestPart("archivo") MultipartFile archivo) {
        return perfil.foto(archivo, true, usuarios.obtenerUsuarioActual());
    }
}
