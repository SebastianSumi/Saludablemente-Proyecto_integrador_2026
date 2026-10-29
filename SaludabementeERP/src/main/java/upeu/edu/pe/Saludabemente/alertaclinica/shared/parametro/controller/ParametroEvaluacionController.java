package upeu.edu.pe.Saludabemente.alertaclinica.shared.parametro.controller;

import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.*;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.service.AsociacionClinicaService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.parametro.dto.ParametroResumenDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.parametro.entity.ParametroEvaluacion;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.parametro.repository.ParametroEvaluacionRepository;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

import java.util.List;

@Tag(name = "Parametros de Evaluacion y asociaciones")
@io.swagger.v3.oas.annotations.responses.ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "JSON, detalle o filtro invalido"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso inexistente"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflicto de negocio, revision desactualizada o bloqueo concurrente")
})
@RestController
@RequestMapping("/api/v1/parametros-evaluacion")
@RequiredArgsConstructor
public class ParametroEvaluacionController {
    private final AsociacionClinicaService asociaciones;


    private static final String ESTADO_ACTIVO = "ACTIVO";
    private static final String ESTADO_INACTIVO = "INACTIVO";

    private final ParametroEvaluacionRepository repository;

    @Operation(summary = "Lista parametros de evaluacion (por defecto solo ACTIVOS)")
    @GetMapping
    public ResponseEntity<List<ParametroResumenDto>> listar(
            @RequestParam(required = false) String estado) {

        String estadoFiltro = ESTADO_ACTIVO;
        if (estado != null && !estado.isBlank()) {
            if (!ESTADO_ACTIVO.equals(estado) && !ESTADO_INACTIVO.equals(estado)) {
                throw new BusinessRuleException(
                        "El estado debe ser ACTIVO o INACTIVO");
            }
            estadoFiltro = estado;
        }

        List<ParametroEvaluacion> parametros = repository.findByEstado(estadoFiltro);

        return ResponseEntity.ok(parametros.stream()
                .map(p -> new ParametroResumenDto(
                        p.getId(), p.getCodigo(), p.getNombre(), p.getUnidad()))
                .toList());
    }

    @Operation(summary = "Consulta un parametro por id")
    @GetMapping("/{id}")
    public ResponseEntity<ParametroResumenDto> obtener(@PathVariable Long id) {
        ParametroEvaluacion p = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Parametro no encontrado: " + id));
        return ResponseEntity.ok(new ParametroResumenDto(
                p.getId(), p.getCodigo(), p.getNombre(), p.getUnidad()));
    }

    @Operation(summary = "Consulta las etiquetas del parametro y la revision de la tarjeta")
    @GetMapping("/{id}/etiquetas")
    public ParametroEtiquetasResponse etiquetas(@PathVariable Long id) { return asociaciones.etiquetasDeParametro(id); }

    @Operation(summary = "Guarda el conjunto final de etiquetas del parametro", description = "Una transaccion. No altera otros parametros de cada etiqueta. Enviar revision del GET.")
    @PutMapping("/{id}/etiquetas")
    public ParametroEtiquetasResponse guardarEtiquetas(@PathVariable Long id, @Valid @RequestBody GuardarEtiquetasRequest request) {
        return asociaciones.guardarEtiquetasDeParametro(id, request);
    }
}
