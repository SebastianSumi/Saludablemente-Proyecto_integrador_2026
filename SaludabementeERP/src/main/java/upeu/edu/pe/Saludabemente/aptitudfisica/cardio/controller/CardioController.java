package upeu.edu.pe.Saludabemente.aptitudfisica.cardio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.aptitudfisica.cardio.dto.request.CardioRequest;
import upeu.edu.pe.Saludabemente.aptitudfisica.cardio.dto.response.CardioResponse;
import upeu.edu.pe.Saludabemente.aptitudfisica.cardio.service.CardioService;

@Tag(name = "Aptitud Física - Cardiovascular")
@RestController
@RequestMapping("/api/v1/aptitud/{idEvaluacion}/cardio")
@RequiredArgsConstructor
public class CardioController {

    private final CardioService service;

    @Operation(summary = "Registra las 2 pruebas cardio (soga 30s, carrera 400m)")
    @PostMapping
    public ResponseEntity<CardioResponse> registrar(
            @PathVariable Long idEvaluacion,
            @Valid @RequestBody CardioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.registrar(idEvaluacion, request));
    }

    @Operation(summary = "Actualiza las 2 pruebas cardio de una evaluación existente")
    @PutMapping
    public ResponseEntity<CardioResponse> actualizar(
            @PathVariable Long idEvaluacion,
            @Valid @RequestBody CardioRequest request) {
        return ResponseEntity.ok(service.actualizar(idEvaluacion, request));
    }

    @Operation(summary = "Consulta el detalle cardiovascular de una evaluación")
    @GetMapping
    public ResponseEntity<CardioResponse> consultar(@PathVariable Long idEvaluacion) {
        return ResponseEntity.ok(service.consultar(idEvaluacion));
    }
}