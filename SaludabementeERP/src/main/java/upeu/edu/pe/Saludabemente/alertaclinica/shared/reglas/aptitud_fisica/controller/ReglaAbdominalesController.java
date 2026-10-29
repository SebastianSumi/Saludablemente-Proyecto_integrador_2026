package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaAbdominalesRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaAbdominalesResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.service.ReglaAbdominalesService;

import java.util.List;

@Tag(name = "Reglas Abdominales (Administracion)")
@RestController
@RequestMapping("/api/v1/reglas/abdominales")
@RequiredArgsConstructor
public class ReglaAbdominalesController {

    private final ReglaAbdominalesService service;

    @Operation(summary = "Lista reglas de abdominales con filtros opcionales")
    @GetMapping
    public ResponseEntity<List<ReglaAbdominalesResponseDto>> listar(
            @RequestParam(required = false) EstadoRegla estado,
            @RequestParam(required = false) SexoBiologico sexo) {
        return ResponseEntity.ok(service.listar(estado, sexo));
    }

    @Operation(summary = "Consulta una regla de abdominales por id")
    @GetMapping("/{id}")
    public ResponseEntity<ReglaAbdominalesResponseDto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @Operation(summary = "Registra una regla de abdominales")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReglaAbdominalesResponseDto crear(
            @Valid @RequestBody ReglaAbdominalesRequestDto requestDto) {
        return service.crear(requestDto);
    }

    @Operation(summary = "Actualiza una regla de abdominales existente")
    @PutMapping("/{id}")
    public ResponseEntity<ReglaAbdominalesResponseDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ReglaAbdominalesRequestDto requestDto) {
        return ResponseEntity.ok(service.actualizar(id, requestDto));
    }

    @Operation(summary = "Desactiva una regla de abdominales")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        service.desactivar(id);
    }

    @Operation(summary = "Reactiva una regla de abdominales")
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ReglaAbdominalesResponseDto> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(service.reactivar(id));
    }
}