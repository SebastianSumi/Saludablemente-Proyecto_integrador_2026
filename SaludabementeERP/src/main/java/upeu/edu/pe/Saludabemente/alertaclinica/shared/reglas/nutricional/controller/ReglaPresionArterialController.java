package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaPresionArterialRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaPresionArterialResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.service.ReglaPresionArterialService;

import java.util.List;

@Tag(name = "Reglas Presion Arterial (Administracion)")
@RestController
@RequestMapping("/api/v1/reglas/presion-arterial")
@RequiredArgsConstructor
public class ReglaPresionArterialController {

    private final ReglaPresionArterialService service;

    @Operation(summary = "Lista reglas de presion arterial con filtros opcionales")
    @GetMapping
    public ResponseEntity<List<ReglaPresionArterialResponseDto>> listar(
            @RequestParam(required = false) EstadoRegla estado,
            @RequestParam(required = false) SexoBiologico sexo) {
        return ResponseEntity.ok(service.listar(estado, sexo));
    }

    @Operation(summary = "Consulta una regla de presion arterial por id")
    @GetMapping("/{id}")
    public ResponseEntity<ReglaPresionArterialResponseDto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @Operation(summary = "Registra una regla de presion arterial")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReglaPresionArterialResponseDto crear(
            @Valid @RequestBody ReglaPresionArterialRequestDto requestDto) {
        return service.crear(requestDto);
    }

    @Operation(summary = "Actualiza una regla de presion arterial existente")
    @PutMapping("/{id}")
    public ResponseEntity<ReglaPresionArterialResponseDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ReglaPresionArterialRequestDto requestDto) {
        return ResponseEntity.ok(service.actualizar(id, requestDto));
    }

    @Operation(summary = "Desactiva una regla de presion arterial")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        service.desactivar(id);
    }

    @Operation(summary = "Reactiva una regla de presion arterial")
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ReglaPresionArterialResponseDto> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(service.reactivar(id));
    }
}