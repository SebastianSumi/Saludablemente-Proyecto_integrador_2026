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
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaMasaMuscularRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaMasaMuscularResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.service.ReglaMasaMuscularService;

import java.util.List;

@Tag(name = "Reglas Masa Muscular (Administracion)")
@RestController
@RequestMapping("/api/v1/reglas/masa-muscular")
@RequiredArgsConstructor
public class ReglaMasaMuscularController {

    private final ReglaMasaMuscularService service;

    @Operation(summary = "Lista reglas de masa muscular con filtros opcionales")
    @GetMapping
    public ResponseEntity<List<ReglaMasaMuscularResponseDto>> listar(
            @RequestParam(required = false) EstadoRegla estado,
            @RequestParam(required = false) SexoBiologico sexo) {
        return ResponseEntity.ok(service.listar(estado, sexo));
    }

    @Operation(summary = "Consulta una regla de masa muscular por id")
    @GetMapping("/{id}")
    public ResponseEntity<ReglaMasaMuscularResponseDto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @Operation(summary = "Registra una regla de masa muscular")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReglaMasaMuscularResponseDto crear(
            @Valid @RequestBody ReglaMasaMuscularRequestDto requestDto) {
        return service.crear(requestDto);
    }

    @Operation(summary = "Actualiza una regla de masa muscular existente")
    @PutMapping("/{id}")
    public ResponseEntity<ReglaMasaMuscularResponseDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ReglaMasaMuscularRequestDto requestDto) {
        return ResponseEntity.ok(service.actualizar(id, requestDto));
    }

    @Operation(summary = "Desactiva una regla de masa muscular")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        service.desactivar(id);
    }

    @Operation(summary = "Reactiva una regla de masa muscular")
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ReglaMasaMuscularResponseDto> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(service.reactivar(id));
    }
}