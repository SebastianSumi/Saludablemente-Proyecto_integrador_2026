package upeu.edu.pe.Saludabemente.noticias.noticia.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.noticias.noticia.dto.NoticiaFeedResponseDTO;
import upeu.edu.pe.Saludabemente.noticias.noticia.dto.NoticiaResponseDTO;
import upeu.edu.pe.Saludabemente.noticias.noticia.service.NoticiaService;

@Tag(name = "Noticias - Público")
@RestController
@RequestMapping("/api/v1/noticias")
@RequiredArgsConstructor
public class NoticiaPublicController {

    private final NoticiaService noticiaService;

    @Operation(summary = "Feed público de noticias publicadas, filtrable por categoría")
    @GetMapping("/feed")
    public ResponseEntity<Page<NoticiaFeedResponseDTO>> feed(
            @RequestParam(required = false) Long idCategoria,
            @RequestParam(required = false) Long idPersona,
            @ParameterObject
            @PageableDefault(size = 20, sort = "fechaPublicacion", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(
                noticiaService.listarFeedPublico(idCategoria, idPersona, pageable));
    }

    @Operation(summary = "Detalle de una noticia publicada")
    @GetMapping("/{id}")
    public ResponseEntity<NoticiaResponseDTO> detalle(
            @PathVariable Long id,
            @RequestParam(required = false) Long idPersona) {
        return ResponseEntity.ok(noticiaService.obtenerDetallePublico(id, idPersona));
    }
}