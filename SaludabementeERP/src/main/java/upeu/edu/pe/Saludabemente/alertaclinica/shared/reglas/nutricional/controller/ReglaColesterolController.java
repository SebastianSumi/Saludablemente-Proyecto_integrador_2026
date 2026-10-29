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
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaColesterolRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaColesterolResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.service.ReglaColesterolService;

import java.util.List;

@Tag(name = "Reglas Colesterol (Administracion)")
@RestController
@RequestMapping("/api/v1/reglas/colesterol")
@RequiredArgsConstructor
public class ReglaColesterolController {

    private final ReglaColesterolService service;

    @Operation(summary = "Lista reglas de colesterol con filtros opcionales")
    @GetMapping
    public ResponseEntity<List<ReglaColesterolResponseDto>> listar(
            @RequestParam(required = false) EstadoRegla estado,
            @RequestParam(required = false) SexoBiologico sexo) {
        return ResponseEntity.ok(service.listar(estado, sexo));
    }

    @Operation(summary = "Consulta una regla de colesterol por id")
    @GetMapping("/{id}")
    public ResponseEntity<ReglaColesterolResponseDto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @Operation(summary = "Registra una regla de colesterol")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReglaColesterolResponseDto crear(
            @Valid @RequestBody ReglaColesterolRequestDto requestDto) {
        return service.crear(requestDto);
    }

    @Operation(summary = "Actualiza una regla de colesterol existente")
    @PutMapping("/{id}")
    public ResponseEntity<ReglaColesterolResponseDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ReglaColesterolRequestDto requestDto) {
        return ResponseEntity.ok(service.actualizar(id, requestDto));
    }

    @Operation(summary = "Desactiva una regla de colesterol")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        service.desactivar(id);
    }

    @Operation(summary = "Reactiva una regla de colesterol")
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ReglaColesterolResponseDto> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(service.reactivar(id));
    }
}