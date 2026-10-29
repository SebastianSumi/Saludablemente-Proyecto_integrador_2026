package upeu.edu.pe.Saludabemente.actividades.configuracion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.actividades.configuracion.dto.request.GamificacionRequest;
import upeu.edu.pe.Saludabemente.actividades.configuracion.dto.request.MaterialesRequest;
import upeu.edu.pe.Saludabemente.actividades.configuracion.dto.response.ConfiguracionResponse;
import upeu.edu.pe.Saludabemente.actividades.configuracion.service.ConfiguracionService;

@Tag(name = "Actividades - Configuracion")
@RestController
@RequestMapping("/api/v1/configuracion")
@RequiredArgsConstructor
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    @Operation(summary = "Gestionar materiales requeridos de una actividad")
    @PostMapping("/{id}/materiales")
    public ResponseEntity<ConfiguracionResponse> gestionarMateriales(
            @PathVariable Long id,
            @Valid @RequestBody MaterialesRequest req) {
        return ResponseEntity.ok(configuracionService.gestionarMateriales(id, req));
    }

    @Operation(summary = "Configurar Health Points (puntaje de salud) de una actividad")
    @PutMapping("/{id}/puntaje")
    public ResponseEntity<ConfiguracionResponse> configurarPuntaje(
            @PathVariable Long id,
            @Valid @RequestBody GamificacionRequest req) {
        return ResponseEntity.ok(configuracionService.configurarPuntaje(id, req));
    }

    @Operation(summary = "Obtener configuracion completa de una actividad")
    @GetMapping("/{id}")
    public ResponseEntity<ConfiguracionResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(configuracionService.obtenerConfiguracion(id));
    }
}