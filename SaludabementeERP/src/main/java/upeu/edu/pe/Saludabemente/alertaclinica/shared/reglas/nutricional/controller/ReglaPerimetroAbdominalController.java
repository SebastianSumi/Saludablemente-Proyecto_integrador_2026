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
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaPerimetroAbdominalRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaPerimetroAbdominalResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.service.ReglaPerimetroAbdominalService;

import java.util.List;

@Tag(name = "Reglas Perimetro Abdominal (Administracion)")
@RestController
@RequestMapping("/api/v1/reglas/perimetro-abdominal")
@RequiredArgsConstructor
public class ReglaPerimetroAbdominalController {

    private final ReglaPerimetroAbdominalService service;

    @Operation(summary = "Lista reglas de perimetro abdominal con filtros opcionales")
    @GetMapping
    public ResponseEntity<List<ReglaPerimetroAbdominalResponseDto>> listar(
            @RequestParam(required = false) EstadoRegla estado,
            @RequestParam(required = false) SexoBiologico sexo) {
        return ResponseEntity.ok(service.listar(estado, sexo));
    }

    @Operation(summary = "Consulta una regla de perimetro abdominal por id")
    @GetMapping("/{id}")
    public ResponseEntity<ReglaPerimetroAbdominalResponseDto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @Operation(summary = "Registra una regla de perimetro abdominal")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReglaPerimetroAbdominalResponseDto crear(
            @Valid @RequestBody ReglaPerimetroAbdominalRequestDto requestDto) {
        return service.crear(requestDto);
    }

    @Operation(summary = "Actualiza una regla de perimetro abdominal existente")
    @PutMapping("/{id}")
    public ResponseEntity<ReglaPerimetroAbdominalResponseDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ReglaPerimetroAbdominalRequestDto requestDto) {
        return ResponseEntity.ok(service.actualizar(id, requestDto));
    }

    @Operation(summary = "Desactiva una regla de perimetro abdominal")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        service.desactivar(id);
    }

    @Operation(summary = "Reactiva una regla de perimetro abdominal")
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ReglaPerimetroAbdominalResponseDto> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(service.reactivar(id));
    }
}