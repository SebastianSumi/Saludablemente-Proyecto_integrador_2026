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
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaSaltaSogaRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaSaltaSogaResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.service.ReglaSaltaSogaService;

import java.util.List;

@Tag(name = "Reglas Salta Soga (Administracion)")
@RestController
@RequestMapping("/api/v1/reglas/salta-soga")
@RequiredArgsConstructor
public class ReglaSaltaSogaController {

    private final ReglaSaltaSogaService service;

    @Operation(summary = "Lista reglas de salta soga con filtros opcionales")
    @GetMapping
    public ResponseEntity<List<ReglaSaltaSogaResponseDto>> listar(
            @RequestParam(required = false) EstadoRegla estado,
            @RequestParam(required = false) SexoBiologico sexo) {
        return ResponseEntity.ok(service.listar(estado, sexo));
    }

    @Operation(summary = "Consulta una regla de salta soga por id")
    @GetMapping("/{id}")
    public ResponseEntity<ReglaSaltaSogaResponseDto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @Operation(summary = "Registra una regla de salta soga")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReglaSaltaSogaResponseDto crear(
            @Valid @RequestBody ReglaSaltaSogaRequestDto requestDto) {
        return service.crear(requestDto);
    }

    @Operation(summary = "Actualiza una regla de salta soga existente")
    @PutMapping("/{id}")
    public ResponseEntity<ReglaSaltaSogaResponseDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ReglaSaltaSogaRequestDto requestDto) {
        return ResponseEntity.ok(service.actualizar(id, requestDto));
    }

    @Operation(summary = "Desactiva una regla de salta soga")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        service.desactivar(id);
    }

    @Operation(summary = "Reactiva una regla de salta soga")
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ReglaSaltaSogaResponseDto> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(service.reactivar(id));
    }
}