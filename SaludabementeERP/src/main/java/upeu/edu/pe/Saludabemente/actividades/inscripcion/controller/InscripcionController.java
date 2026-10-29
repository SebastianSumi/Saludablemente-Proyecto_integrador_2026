package upeu.edu.pe.Saludabemente.actividades.inscripcion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.actividades.inscripcion.dto.request.InscribirPersonasRequest;
import upeu.edu.pe.Saludabemente.actividades.inscripcion.service.InscripcionService;

import java.util.List;
import java.util.Map;

@Tag(name = "Actividades - Inscripciones")
@RestController
@RequestMapping("/api/v1/actividades/{idActividad}/inscripciones")
@RequiredArgsConstructor
public class InscripcionController {

    private final InscripcionService inscripcionService;

    @Operation(summary = "Inscribir a TODAS las personas activas (reemplaza inscritos previos)")
    @PostMapping("/todos")
    public ResponseEntity<Map<String, Object>> inscribirATodos(@PathVariable Long idActividad) {
        int total = inscripcionService.inscribirATodos(idActividad);
        return ResponseEntity.ok(Map.of(
                "idActividad", idActividad,
                "inscritos", total,
                "modo", "TODOS"
        ));
    }

    @Operation(summary = "Inscribir personas especificas (agrega a los existentes)")
    @PostMapping
    public ResponseEntity<Map<String, Object>> inscribirPersonas(
            @PathVariable Long idActividad,
            @Valid @RequestBody InscribirPersonasRequest req) {
        int agregados = inscripcionService.inscribirPersonas(idActividad, req.idsPersonas());
        return ResponseEntity.ok(Map.of(
                "idActividad", idActividad,
                "agregados", agregados,
                "modo", "ESPECIFICO"
        ));
    }

    @Operation(summary = "Quitar un inscrito")
    @DeleteMapping("/{idPersona}")
    public ResponseEntity<Void> quitar(
            @PathVariable Long idActividad,
            @PathVariable Long idPersona) {

        boolean eliminado = inscripcionService.quitarInscrito(idActividad, idPersona);

        if (!eliminado) {
            // No existia el inscrito: 404
            return ResponseEntity.notFound().build();
        }

        // Se elimino correctamente: 204
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar IDs de inscritos")
    @GetMapping
    public ResponseEntity<List<Long>> listar(@PathVariable Long idActividad) {
        return ResponseEntity.ok(inscripcionService.listarInscritos(idActividad));
    }

    @Operation(summary = "Contar inscritos")
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> contar(@PathVariable Long idActividad) {
        return ResponseEntity.ok(Map.of("total", inscripcionService.contarInscritos(idActividad)));
    }
}