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
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaImcRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaImcResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.service.ReglaImcService;

import java.util.List;

@Tag(name = "Reglas IMC (Administracion)")
@RestController
@RequestMapping("/api/v1/reglas/imc")
@RequiredArgsConstructor
public class ReglaImcController {

    private final ReglaImcService service;

    @Operation(summary = "Lista reglas de IMC con filtros opcionales")
    @GetMapping
    public ResponseEntity<List<ReglaImcResponseDto>> listar(
            @RequestParam(required = false) EstadoRegla estado,
            @RequestParam(required = false) SexoBiologico sexo) {
        return ResponseEntity.ok(service.listar(estado, sexo));
    }

    @Operation(summary = "Consulta una regla de IMC por id")
    @GetMapping("/{id}")
    public ResponseEntity<ReglaImcResponseDto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @Operation(summary = "Registra una regla de IMC")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReglaImcResponseDto crear(@Valid @RequestBody ReglaImcRequestDto requestDto) {
        return service.crear(requestDto);
    }

    @Operation(summary = "Actualiza una regla de IMC existente")
    @PutMapping("/{id}")
    public ResponseEntity<ReglaImcResponseDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ReglaImcRequestDto requestDto) {
        return ResponseEntity.ok(service.actualizar(id, requestDto));
    }

    @Operation(summary = "Desactiva (baja logica) una regla de IMC")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        service.desactivar(id);
    }

    @Operation(summary = "Reactiva una regla de IMC previamente desactivada")
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ReglaImcResponseDto> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(service.reactivar(id));
    }
}