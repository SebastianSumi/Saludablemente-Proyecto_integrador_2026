package upeu.edu.pe.Saludabemente.actividades.imagen.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import upeu.edu.pe.Saludabemente.actividades.imagen.dto.response.ImagenResponse;
import upeu.edu.pe.Saludabemente.actividades.imagen.service.ImagenService;

import java.util.List;
import java.util.Map;

@Tag(name = "Actividades - Imagenes")
@RestController
@RequestMapping("/api/v1/agenda/actividades/{idActividad}")
@RequiredArgsConstructor
public class ImagenController {

    private final ImagenService imagenService;

    // ===== Logo =====

    @Operation(summary = "Subir o reemplazar el logo de la actividad")
    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> subirLogo(
            @PathVariable Long idActividad,
            @RequestParam("archivo") MultipartFile archivo) {
        String url = imagenService.subirLogo(idActividad, archivo);
        return ResponseEntity.ok(Map.of("logoUrl", url));
    }

    @Operation(summary = "Eliminar el logo de la actividad")
    @DeleteMapping("/logo")
    public ResponseEntity<Void> eliminarLogo(@PathVariable Long idActividad) {
        imagenService.eliminarLogo(idActividad);
        return ResponseEntity.noContent().build();
    }

    // ===== Galeria =====

    @Operation(summary = "Subir una imagen a la galeria")
    @PostMapping(value = "/imagenes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImagenResponse> subirImagen(
            @PathVariable Long idActividad,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(value = "descripcion", required = false) String descripcion) {
        return ResponseEntity.ok(imagenService.subirImagen(idActividad, archivo, descripcion));
    }

    @Operation(summary = "Listar imagenes de la galeria")
    @GetMapping("/imagenes")
    public ResponseEntity<List<ImagenResponse>> listar(@PathVariable Long idActividad) {
        return ResponseEntity.ok(imagenService.listarImagenes(idActividad));
    }

    @Operation(summary = "Eliminar una imagen de la galeria")
    @DeleteMapping("/imagenes/{idImagen}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long idActividad,
            @PathVariable Long idImagen) {
        imagenService.eliminarImagen(idActividad, idImagen);
        return ResponseEntity.noContent().build();
    }
}