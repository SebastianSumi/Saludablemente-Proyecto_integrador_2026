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
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaGrasaVisceralRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaGrasaVisceralResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.service.ReglaGrasaVisceralService;

import java.util.List;

@Tag(name = "Reglas Grasa Visceral (Administracion)")
@RestController
@RequestMapping("/api/v1/reglas/grasa-visceral")
@RequiredArgsConstructor
public class ReglaGrasaVisceralController {

    private final ReglaGrasaVisceralService service;

    @Operation(summary = "Lista reglas de grasa visceral con filtros opcionales")
    @GetMapping
    public ResponseEntity<List<ReglaGrasaVisceralResponseDto>> listar(
            @RequestParam(required = false) EstadoRegla estado,
            @RequestParam(required = false) SexoBiologico sexo) {
        return ResponseEntity.ok(service.listar(estado, sexo));
    }

    @Operation(summary = "Consulta una regla de grasa visceral por id")
    @GetMapping("/{id}")
    public ResponseEntity<ReglaGrasaVisceralResponseDto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @Operation(summary = "Registra una regla de grasa visceral")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReglaGrasaVisceralResponseDto crear(
            @Valid @RequestBody ReglaGrasaVisceralRequestDto requestDto) {
        return service.crear(requestDto);
    }

    @Operation(summary = "Actualiza una regla de grasa visceral existente")
    @PutMapping("/{id}")
    public ResponseEntity<ReglaGrasaVisceralResponseDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ReglaGrasaVisceralRequestDto requestDto) {
        return ResponseEntity.ok(service.actualizar(id, requestDto));
    }

    @Operation(summary = "Desactiva una regla de grasa visceral")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        service.desactivar(id);
    }

    @Operation(summary = "Reactiva una regla de grasa visceral")
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ReglaGrasaVisceralResponseDto> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(service.reactivar(id));
    }
}