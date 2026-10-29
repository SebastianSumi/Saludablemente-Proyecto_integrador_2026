package upeu.edu.pe.Saludabemente.noticias.noticia.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import upeu.edu.pe.Saludabemente.noticias.noticia.dto.NoticiaRequestDTO;
import upeu.edu.pe.Saludabemente.noticias.noticia.dto.NoticiaResponseDTO;
import upeu.edu.pe.Saludabemente.noticias.noticia.service.NoticiaService;
import upeu.edu.pe.Saludabemente.noticias.shared.enums.EstadoNoticia;

@Tag(name = "Noticias - Admin")
@RestController
@RequestMapping("/api/v1/noticias/admin")
@RequiredArgsConstructor
public class NoticiaAdminController {

    private final NoticiaService noticiaService;

    @Operation(summary = "Crea un borrador de noticia")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<NoticiaResponseDTO> crear(@Valid @RequestBody NoticiaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(noticiaService.crearBorrador(request));
    }

    @Operation(summary = "Actualiza un borrador existente")
    @PutMapping("/{id}")
    public ResponseEntity<NoticiaResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody NoticiaRequestDTO request) {
        return ResponseEntity.ok(noticiaService.actualizarBorrador(id, request));
    }

    @Operation(summary = "Sube o reemplaza la imagen de portada de una noticia")
    @PostMapping(value = "/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NoticiaResponseDTO> subirImagen(
            @PathVariable Long id,
            @RequestPart("imagen") MultipartFile imagen) {
        return ResponseEntity.ok(noticiaService.subirImagen(id, imagen));
    }

    @Operation(summary = "Elimina la imagen de portada de una noticia")
    @DeleteMapping("/{id}/imagen")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarImagen(@PathVariable Long id) {
        noticiaService.eliminarImagen(id);
    }

    @Operation(summary = "Publica una noticia en borrador")
    @PatchMapping("/{id}/publicar")
    public ResponseEntity<NoticiaResponseDTO> publicar(@PathVariable Long id) {
        return ResponseEntity.ok(noticiaService.publicar(id));
    }

    @Operation(summary = "Despublica una noticia, regresa a borrador")
    @PatchMapping("/{id}/despublicar")
    public ResponseEntity<NoticiaResponseDTO> despublicar(@PathVariable Long id) {
        return ResponseEntity.ok(noticiaService.despublicar(id));
    }

    @Operation(summary = "Archiva una noticia publicada")
    @PatchMapping("/{id}/archivar")
    public ResponseEntity<NoticiaResponseDTO> archivar(@PathVariable Long id) {
        return ResponseEntity.ok(noticiaService.archivar(id));
    }

    @Operation(summary = "Obtiene el detalle completo de una noticia")
    @GetMapping("/{id}")
    public ResponseEntity<NoticiaResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(noticiaService.obtenerDetalleAdmin(id));
    }

    @Operation(summary = "Lista noticias con filtros para el panel admin")
    @GetMapping
    public ResponseEntity<Page<NoticiaResponseDTO>> listar(
            @RequestParam(required = false) EstadoNoticia estado,
            @RequestParam(required = false) Long idCategoria,
            @RequestParam(required = false) Long idArea,
            @RequestParam(required = false) Long idTeam,
            @ParameterObject
            @PageableDefault(size = 20, sort = "fechaCreacion", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(
                noticiaService.listarAdmin(estado, idCategoria, idArea, idTeam, pageable));
    }
}