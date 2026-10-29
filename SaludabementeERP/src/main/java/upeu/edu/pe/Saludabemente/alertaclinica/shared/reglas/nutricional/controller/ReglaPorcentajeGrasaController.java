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
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaPorcentajeGrasaRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaPorcentajeGrasaResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.service.ReglaPorcentajeGrasaService;

import java.util.List;

@Tag(name = "Reglas Porcentaje Grasa (Administracion)")
@RestController
@RequestMapping("/api/v1/reglas/porcentaje-grasa")
@RequiredArgsConstructor
public class ReglaPorcentajeGrasaController {

    private final ReglaPorcentajeGrasaService service;

    @Operation(summary = "Lista reglas de porcentaje de grasa con filtros opcionales")
    @GetMapping
    public ResponseEntity<List<ReglaPorcentajeGrasaResponseDto>> listar(
            @RequestParam(required = false) EstadoRegla estado,
            @RequestParam(required = false) SexoBiologico sexo) {
        return ResponseEntity.ok(service.listar(estado, sexo));
    }

    @Operation(summary = "Consulta una regla de porcentaje de grasa por id")
    @GetMapping("/{id}")
    public ResponseEntity<ReglaPorcentajeGrasaResponseDto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @Operation(summary = "Registra una regla de porcentaje de grasa")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReglaPorcentajeGrasaResponseDto crear(
            @Valid @RequestBody ReglaPorcentajeGrasaRequestDto requestDto) {
        return service.crear(requestDto);
    }

    @Operation(summary = "Actualiza una regla de porcentaje de grasa existente")
    @PutMapping("/{id}")
    public ResponseEntity<ReglaPorcentajeGrasaResponseDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ReglaPorcentajeGrasaRequestDto requestDto) {
        return ResponseEntity.ok(service.actualizar(id, requestDto));
    }

    @Operation(summary = "Desactiva una regla de porcentaje de grasa")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        service.desactivar(id);
    }

    @Operation(summary = "Reactiva una regla de porcentaje de grasa")
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ReglaPorcentajeGrasaResponseDto> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(service.reactivar(id));
    }
}