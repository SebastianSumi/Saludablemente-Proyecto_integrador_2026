package upeu.edu.pe.Saludabemente.social.analitica.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import upeu.edu.pe.Saludabemente.social.post.dto.MetricaResponse;
import upeu.edu.pe.Saludabemente.social.analitica.service.AnaliticaService;
import upeu.edu.pe.Saludabemente.social.integracion.client.SocialUsuarioClientService;
import java.time.LocalDate;

@RestController @RequestMapping("/api/v1/social/admin/metricas") @RequiredArgsConstructor @Tag(name="Social - Analítica (Admin)")
public class AnaliticaController {
    private final AnaliticaService analitica;private final SocialUsuarioClientService usuarios;
    @GetMapping public MetricaResponse metricas(@RequestParam LocalDate desde,@RequestParam LocalDate hasta) {return analitica.metricas(desde,hasta,usuarios.obtenerUsuarioActual());}
}
