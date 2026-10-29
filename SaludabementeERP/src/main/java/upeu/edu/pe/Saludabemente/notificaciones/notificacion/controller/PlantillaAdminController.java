package upeu.edu.pe.Saludabemente.notificaciones.notificacion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto.PlantillaRequest;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto.PlantillaResponse;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto.PlantillaResumen;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.service.plantilla.PlantillaService;

import java.util.List;

@Tag(name = "Plantillas")
@RestController
@RequestMapping("/api/v1/notificaciones/plantillas")
@RequiredArgsConstructor
public class PlantillaAdminController {

    private final PlantillaService plantillaService;

    @Operation(summary = "Lista todas las plantillas registradas (sin cuerpo HTML)")
    @GetMapping
    public ResponseEntity<List<PlantillaResumen>> listar() {
        return ResponseEntity.ok(plantillaService.listar());
    }

    @Operation(summary = "Consulta una plantilla por id (con cuerpo HTML)")
    @GetMapping("/{id}")
    public ResponseEntity<PlantillaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(plantillaService.obtener(id));
    }

    @Operation(summary = "Registra una plantilla nueva")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlantillaResponse crear(@Valid @RequestBody PlantillaRequest request) {
        return plantillaService.crear(request);
    }

    @Operation(summary = "Actualiza una plantilla existente")
    @PutMapping("/{id}")
    public ResponseEntity<PlantillaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PlantillaRequest request) {
        return ResponseEntity.ok(plantillaService.actualizar(id, request));
    }

    @Operation(summary = "Elimina una plantilla")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        plantillaService.eliminar(id);
    }
}