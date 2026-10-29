package upeu.edu.pe.Saludabemente.social.busqueda.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;
import io.swagger.v3.oas.annotations.tags.Tag;
import upeu.edu.pe.Saludabemente.social.post.dto.*;
import upeu.edu.pe.Saludabemente.social.post.service.SocialConsultaService;
import upeu.edu.pe.Saludabemente.social.integracion.client.SocialUsuarioClientService;
import upeu.edu.pe.Saludabemente.social.api.dto.TipoMencion;
import java.util.List;

@RestController @RequestMapping("/api/v1/social") @RequiredArgsConstructor @Tag(name="Social - Búsqueda y Menciones")
public class BusquedaController {
    private final SocialConsultaService consulta;private final SocialUsuarioClientService usuarios;
    @GetMapping("/buscar") public BusquedaResponse buscar(@RequestParam String q,@RequestParam(defaultValue="POST") String tipo,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {var u=usuarios.obtenerUsuarioActual();return consulta.buscar(q,tipo,consulta.pagina(page,size),u);}
    @GetMapping("/menciones/buscar") public Object autocomplete(@RequestParam TipoMencion tipo,@RequestParam String q,@RequestParam(defaultValue="5") int limite) {usuarios.obtenerUsuarioActual();return consulta.autocomplete(tipo,q,limite);}
    @GetMapping("/hashtags/tendencia") public List<TopHashtag> tendencias(@RequestParam(defaultValue="7") int dias,@RequestParam(defaultValue="10") int limite) {usuarios.obtenerUsuarioActual();return consulta.tendencias(dias,limite);}
}
