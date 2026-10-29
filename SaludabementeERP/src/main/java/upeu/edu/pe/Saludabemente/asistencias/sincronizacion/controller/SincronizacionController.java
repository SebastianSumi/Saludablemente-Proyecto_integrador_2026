package upeu.edu.pe.Saludabemente.asistencias.sincronizacion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.asistencias.sincronizacion.dto.request.LoteRequest;
import upeu.edu.pe.Saludabemente.asistencias.sincronizacion.dto.response.LoteResponse;
import upeu.edu.pe.Saludabemente.asistencias.sincronizacion.service.SincronizacionService;

import java.util.Map;

@Tag(name = "Sincronización", description = "Motor offline y carga masiva")
@RestController
@RequestMapping("/api/v1/sincronizacion")
@RequiredArgsConstructor
public class SincronizacionController {

    private final SincronizacionService sincronizacionService;

    @Operation(summary = "Health check para detectar conexión")
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", System.currentTimeMillis()
        ));
    }

    @Operation(summary = "Procesar lote de asistencias offline")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lote procesado"),
            @ApiResponse(responseCode = "400", description = "Lote vacío o malformado")
    })
    @PostMapping("/lote")
    public ResponseEntity<LoteResponse> procesarLote(
            @Valid @RequestBody LoteRequest request) {
        return ResponseEntity.ok(sincronizacionService.procesarLote(request));
    }
}