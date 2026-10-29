package upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.dto.request.FuerzaRequest;
import upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.dto.response.FuerzaResponse;
import upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.service.FuerzaService;

@Tag(name = "Aptitud Física - Fuerza")
@RestController
@RequestMapping("/api/v1/aptitud/{idEvaluacion}/fuerza")
@RequiredArgsConstructor
public class FuerzaController {

    private final FuerzaService service;

    @Operation(summary = "Registra las 3 pruebas de fuerza (abdominales, planchas, salto)")
    @PostMapping
    public ResponseEntity<FuerzaResponse> registrar(
            @PathVariable Long idEvaluacion,
            @Valid @RequestBody FuerzaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.registrar(idEvaluacion, request));
    }

    @Operation(summary = "Actualiza las 3 pruebas de fuerza de una evaluación existente")
    @PutMapping
    public ResponseEntity<FuerzaResponse> actualizar(
            @PathVariable Long idEvaluacion,
            @Valid @RequestBody FuerzaRequest request) {
        return ResponseEntity.ok(service.actualizar(idEvaluacion, request));
    }

    @Operation(summary = "Consulta el detalle de fuerza de una evaluación")
    @GetMapping
    public ResponseEntity<FuerzaResponse> consultar(@PathVariable Long idEvaluacion) {
        return ResponseEntity.ok(service.consultar(idEvaluacion));
    }
}