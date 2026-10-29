package upeu.edu.pe.Saludabemente.nutricional.antropometrico.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import upeu.edu.pe.Saludabemente.nutricional.antropometrico.dto.request.AntropometricoRequest;
import upeu.edu.pe.Saludabemente.nutricional.antropometrico.dto.response.AntropometricoResponse;
import upeu.edu.pe.Saludabemente.nutricional.antropometrico.service.AntropometricoService;

@Tag(name = "Antropometría",
        description = "Medidas físicas y diagnósticos OMRON")
@RestController
@RequestMapping("/api/v1/evaluaciones-nutricionales/{idEvaluacion}/antropometrico")
@RequiredArgsConstructor
public class AntropometricoController {

    private final AntropometricoService antropometricoService;

    // ===================================================================
    // POST /{idEvaluacion}/antropometrico
    // ===================================================================

    @Operation(summary = "Registra las medidas físicas y calcula diagnósticos OMRON")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Antropometría registrada"),
            @ApiResponse(responseCode = "400", description = "Datos fuera de rango plausible"),
            @ApiResponse(responseCode = "404", description = "Evaluación inexistente"),
            @ApiResponse(responseCode = "409", description = "Evaluación no PENDIENTE o ya tiene antropometría")
    })
    @PostMapping
    public ResponseEntity<AntropometricoResponse> registrar(
            @PathVariable Long idEvaluacion,
            @Valid @RequestBody AntropometricoRequest request) {

        AntropometricoResponse creada =
                antropometricoService.registrar(idEvaluacion, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    // ===================================================================
    // PUT /{idEvaluacion}/antropometrico/recalcular
    // ===================================================================

    @Operation(summary = "Recalcula diagnósticos OMRON sobre las medidas ya guardadas",
            description = "Uso: RRHH corrigió la fecha de nacimiento o el sexo de la persona")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Diagnósticos recalculados"),
            @ApiResponse(responseCode = "404", description = "Evaluación o antropometría inexistente"),
            @ApiResponse(responseCode = "409", description = "Evaluación anulada")
    })
    @PutMapping("/recalcular")
    public ResponseEntity<AntropometricoResponse> recalcular(
            @PathVariable Long idEvaluacion) {

        return ResponseEntity.ok(antropometricoService.recalcular(idEvaluacion));
    }

    @Operation(summary = "Actualiza las medidas físicas (corrige errores de tipeo)",
            description = "Permitido en PENDIENTE y COMPLETA. Bloqueado en ANULADA.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Antropometría actualizada"),
            @ApiResponse(responseCode = "404", description = "Evaluación o antropometría inexistente"),
            @ApiResponse(responseCode = "409", description = "Evaluación anulada o valores fuera de rango")
    })
    @PutMapping
    public ResponseEntity<AntropometricoResponse> actualizar(
            @PathVariable Long idEvaluacion,
            @Valid @RequestBody AntropometricoRequest request) {

        return ResponseEntity.ok(
                antropometricoService.actualizar(idEvaluacion, request));
    }
}