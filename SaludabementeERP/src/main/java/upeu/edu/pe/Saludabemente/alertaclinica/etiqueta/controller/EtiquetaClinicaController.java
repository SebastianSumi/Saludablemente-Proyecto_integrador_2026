package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.controller;

import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.*;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.service.AsociacionClinicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.EtiquetaClinicaRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.EtiquetaClinicaResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.service.EtiquetaClinicaService;

import java.util.List;

@Tag(name = "Etiquetas Clinicas (Administracion)")
@io.swagger.v3.oas.annotations.responses.ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "JSON, detalle o filtro invalido"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso inexistente"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflicto de negocio, revision desactualizada o bloqueo concurrente")
})
@RestController
@RequestMapping("/api/v1/etiquetas-clinicas")
@RequiredArgsConstructor
public class EtiquetaClinicaController {
    private final AsociacionClinicaService asociaciones;
    private final upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.service.ConsultaEtiquetaService consultas;


    private final EtiquetaClinicaService service;

    @Operation(summary = "Lista etiquetas clinicas, filtradas opcionalmente por estado")
    @GetMapping
    public ResponseEntity<List<EtiquetaClinicaResponseDto>> listar(
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(service.listar(estado));
    }

    @Operation(summary = "Consulta una etiqueta clinica por id")
    @GetMapping("/{id}")
    public ResponseEntity<EtiquetaClinicaResponseDto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @Operation(summary = "Registra una etiqueta clinica")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EtiquetaClinicaResponseDto crear(
            @Valid @RequestBody EtiquetaClinicaRequestDto dto) {
        return service.crear(dto);
    }

    @Operation(summary = "Actualiza una etiqueta clinica existente (el codigo es inmutable)")
    @PutMapping("/{id}")
    public ResponseEntity<EtiquetaClinicaResponseDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EtiquetaClinicaRequestDto dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @Operation(summary = "Desactiva (baja logica) una etiqueta clinica")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        service.desactivar(id);
    }

    @Operation(summary = "Reactiva una etiqueta clinica previamente desactivada")
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<EtiquetaClinicaResponseDto> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(service.reactivar(id));
    }

    @Operation(summary = "Consulta la tarjeta de parametros y su revision")
    @GetMapping("/{id}/parametros")
    public EtiquetaParametrosResponse parametros(@PathVariable Long id) { return asociaciones.parametrosDeEtiqueta(id); }

    @Operation(summary = "Guarda el conjunto final de parametros en una transaccion", description = "Enviar revision del GET y detalles completos. Lista vacia permitida; duplicados o revision obsoleta: 409.")
    @PutMapping("/{id}/parametros")
    public EtiquetaParametrosResponse guardarParametros(@PathVariable Long id, @Valid @RequestBody GuardarParametrosRequest request) {
        return asociaciones.guardarParametrosDeEtiqueta(id, request);
    }

    @Operation(summary = "Consulta la tarjeta de alertas asociadas y su revision")
    @GetMapping("/{id}/alertas")
    public EtiquetaAlertasResponse alertas(@PathVariable Long id) { return asociaciones.alertasDeEtiqueta(id); }

    @Operation(summary = "Guarda el conjunto final de alertas existentes en una transaccion", description = "No crea alertas. Las asociaciones MOTOR deben conservarse. Nuevas asignaciones MANUAL. revision obsoleta: 409.")
    @PutMapping("/{id}/alertas")
    public EtiquetaAlertasResponse guardarAlertas(@PathVariable Long id, @Valid @RequestBody GuardarAlertasRequest request) {
        return asociaciones.guardarAlertasDeEtiqueta(id, request);
    }

    @Operation(summary = "Busca etiquetas resumidas y paginadas para listados y selectores")
    @GetMapping("/consulta")
    public org.springframework.data.domain.Page<EtiquetaResumenDto> consulta(
            @RequestParam(required = false) String estado, @RequestParam(required = false) String texto,
            @RequestParam(required = false) Long idParametro, @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio) {
        return consultas.buscar(estado, texto, idParametro, pagina, tamanio);
    }

    @Operation(summary = "Reporte de etiquetas con parametros, alertas y agregados sin duplicar alertas",
            description = "desde/hasta filtran fecha de asignacion, ambos limites inclusivos. Incluye etiquetas sin asignaciones; sus contadores son cero.")
    @GetMapping("/reporte")
    public ReporteEtiquetasResponse reporte(
            @RequestParam(required = false) String estadoEtiqueta,
            @RequestParam(required = false) Long idEtiqueta,
            @RequestParam(required = false) Long idParametro,
            @RequestParam(required = false) upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.EstadoAlerta estadoAlerta,
            @RequestParam(required = false) upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.SeveridadAlerta severidad,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime desde,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime hasta) {
        return consultas.reporte(estadoEtiqueta, idEtiqueta, idParametro, estadoAlerta, severidad, desde, hasta);
    }
}
