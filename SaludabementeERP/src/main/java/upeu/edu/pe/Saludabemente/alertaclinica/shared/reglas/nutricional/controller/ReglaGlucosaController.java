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
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaGlucosaRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaGlucosaResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.service.ReglaGlucosaService;

import java.util.List;

@Tag(name = "Reglas Glucosa (Administracion)")
@RestController
@RequestMapping("/api/v1/reglas/glucosa")
@RequiredArgsConstructor
public class ReglaGlucosaController {

    private final ReglaGlucosaService service;

    @Operation(summary = "Lista reglas de glucosa con filtros opcionales")
    @GetMapping
    public ResponseEntity<List<ReglaGlucosaResponseDto>> listar(
            @RequestParam(required = false) EstadoRegla estado,
            @RequestParam(required = false) SexoBiologico sexo) {
        return ResponseEntity.ok(service.listar(estado, sexo));
    }

    @Operation(summary = "Consulta una regla de glucosa por id")
    @GetMapping("/{id}")
    public ResponseEntity<ReglaGlucosaResponseDto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @Operation(summary = "Registra una regla de glucosa")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReglaGlucosaResponseDto crear(@Valid @RequestBody ReglaGlucosaRequestDto requestDto) {
        return service.crear(requestDto);
    }

    @Operation(summary = "Actualiza una regla de glucosa existente")
    @PutMapping("/{id}")
    public ResponseEntity<ReglaGlucosaResponseDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ReglaGlucosaRequestDto requestDto) {
        return ResponseEntity.ok(service.actualizar(id, requestDto));
    }

    @Operation(summary = "Desactiva una regla de glucosa")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        service.desactivar(id);
    }

    @Operation(summary = "Reactiva una regla de glucosa")
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ReglaGlucosaResponseDto> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(service.reactivar(id));
    }
}