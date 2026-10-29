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
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaTrigliceridosRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaTrigliceridosResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.service.ReglaTrigliceridosService;

import java.util.List;

@Tag(name = "Reglas Trigliceridos (Administracion)")
@RestController
@RequestMapping("/api/v1/reglas/trigliceridos")
@RequiredArgsConstructor
public class ReglaTrigliceridosController {

    private final ReglaTrigliceridosService service;

    @Operation(summary = "Lista reglas de trigliceridos con filtros opcionales")
    @GetMapping
    public ResponseEntity<List<ReglaTrigliceridosResponseDto>> listar(
            @RequestParam(required = false) EstadoRegla estado,
            @RequestParam(required = false) SexoBiologico sexo) {
        return ResponseEntity.ok(service.listar(estado, sexo));
    }

    @Operation(summary = "Consulta una regla de trigliceridos por id")
    @GetMapping("/{id}")
    public ResponseEntity<ReglaTrigliceridosResponseDto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @Operation(summary = "Registra una regla de trigliceridos")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReglaTrigliceridosResponseDto crear(
            @Valid @RequestBody ReglaTrigliceridosRequestDto requestDto) {
        return service.crear(requestDto);
    }

    @Operation(summary = "Actualiza una regla de trigliceridos existente")
    @PutMapping("/{id}")
    public ResponseEntity<ReglaTrigliceridosResponseDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ReglaTrigliceridosRequestDto requestDto) {
        return ResponseEntity.ok(service.actualizar(id, requestDto));
    }

    @Operation(summary = "Desactiva una regla de trigliceridos")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        service.desactivar(id);
    }

    @Operation(summary = "Reactiva una regla de trigliceridos")
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ReglaTrigliceridosResponseDto> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(service.reactivar(id));
    }
}