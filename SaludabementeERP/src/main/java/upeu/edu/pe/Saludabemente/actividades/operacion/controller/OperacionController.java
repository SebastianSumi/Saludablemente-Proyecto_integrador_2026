package upeu.edu.pe.Saludabemente.actividades.operacion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.actividades.agenda.dto.response.ActividadResponse;
import upeu.edu.pe.Saludabemente.actividades.operacion.dto.request.CancelacionRequest;
import upeu.edu.pe.Saludabemente.actividades.operacion.dto.request.ReprogramacionRequest;
import upeu.edu.pe.Saludabemente.actividades.operacion.dto.request.TransferenciaRequest;
import upeu.edu.pe.Saludabemente.actividades.operacion.service.OperacionService;

@Tag(name = "Actividades - Operacion")
@RestController
@RequestMapping("/api/v1/operacion")
@RequiredArgsConstructor
public class OperacionController {

    private final OperacionService operacionService;

    @Operation(summary = "Reprogramar una actividad PROGRAMADA")
    @PutMapping("/{id}/tiempo")
    public ResponseEntity<ActividadResponse> reprogramar(
            @PathVariable Long id,
            @Valid @RequestBody ReprogramacionRequest req) {
        return ResponseEntity.ok(operacionService.reprogramar(id, req));
    }

    @Operation(summary = "Cancelar una actividad con motivo obligatorio")
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ActividadResponse> cancelar(
            @PathVariable Long id,
            @Valid @RequestBody CancelacionRequest req) {
        return ResponseEntity.ok(operacionService.cancelar(id, req));
    }

    @Operation(summary = "Iniciar una actividad (PROGRAMADA -> EN_CURSO)")
    @PatchMapping("/{id}/iniciar")
    public ResponseEntity<ActividadResponse> iniciar(@PathVariable Long id) {
        return ResponseEntity.ok(operacionService.iniciar(id));
    }

    @Operation(summary = "Finalizar una actividad (EN_CURSO -> FINALIZADA)")
    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<ActividadResponse> finalizar(@PathVariable Long id) {
        return ResponseEntity.ok(operacionService.finalizar(id));
    }

    @Operation(summary = "Transferir el coordinador de una actividad")
    @PutMapping("/{id}/coordinador")
    public ResponseEntity<ActividadResponse> transferir(
            @PathVariable Long id,
            @Valid @RequestBody TransferenciaRequest req) {
        return ResponseEntity.ok(operacionService.transferirCoordinador(id, req));
    }
}