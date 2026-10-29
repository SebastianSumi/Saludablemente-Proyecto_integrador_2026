package upeu.edu.pe.Saludabemente.noticias.categoria.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import upeu.edu.pe.Saludabemente.noticias.categoria.dto.CategoriaResponseDTO;
import upeu.edu.pe.Saludabemente.noticias.categoria.service.CategoriaService;

import java.util.List;

@Tag(name = "Noticias - Categorías (Público)")
@RestController
@RequestMapping("/api/v1/noticias/categorias/public")
@RequiredArgsConstructor
public class CategoriaPublicController {

    private final CategoriaService categoriaService;

    @Operation(summary = "Lista las categorías activas para el feed")
    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> listarActivas() {
        return ResponseEntity.ok(categoriaService.listarActivas());
    }
}