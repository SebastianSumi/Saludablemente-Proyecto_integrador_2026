package upeu.edu.pe.Saludabemente.noticias.categoria.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.noticias.categoria.dto.CategoriaRequestDTO;
import upeu.edu.pe.Saludabemente.noticias.categoria.dto.CategoriaResponseDTO;
import upeu.edu.pe.Saludabemente.noticias.categoria.service.CategoriaService;

import java.util.List;

@Tag(name = "Noticias - Categorías (Admin)")
@RestController
@RequestMapping("/api/v1/noticias/categorias")
@RequiredArgsConstructor
public class CategoriaAdminController {

    private final CategoriaService categoriaService;

    @Operation(summary = "Lista todas las categorías, incluidas inactivas")
    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(categoriaService.listarTodas());
    }

    @Operation(summary = "Obtiene una categoría por id")
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.obtener(id));
    }

    @Operation(summary = "Crea una categoría nueva")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CategoriaResponseDTO> crear(@Valid @RequestBody CategoriaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.crear(request));
    }

    @Operation(summary = "Actualiza una categoría existente")
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequestDTO request) {
        return ResponseEntity.ok(categoriaService.actualizar(id, request));
    }

    @Operation(summary = "Activa una categoría inactiva")
    @PatchMapping("/{id}/activar")
    public ResponseEntity<CategoriaResponseDTO> activar(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.activar(id));
    }

    @Operation(summary = "Inactiva una categoría activa")
    @PatchMapping("/{id}/inactivar")
    public ResponseEntity<CategoriaResponseDTO> inactivar(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.inactivar(id));
    }

    @Operation(summary = "Elimina una categoría. Solo si no es del sistema y no tiene noticias")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
    }
}