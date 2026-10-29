package upeu.edu.pe.Saludabemente.nutricional.bioquimico.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.dto.ImportacionReporteDto;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.dto.request.BioquimicoManualRequest;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.dto.response.BioquimicoResponse;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.service.BioquimicoService;

@Tag(name = "Bioquímica",
        description = "Registro manual e importación masiva del laboratorio")
@RestController
@RequestMapping("/api/v1/evaluaciones-nutricionales")
@RequiredArgsConstructor
public class BioquimicoController {

    private final BioquimicoService bioquimicoService;

    // ===================================================================
    // POST /importar-bioquimico
    // ===================================================================

    @Operation(summary = "Importa el Excel del laboratorio para un periodo",
            description = "Cruce por celular. Filas sin evaluación PENDIENTE se rechazan "
                    + "y se devuelven en el reporte.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Importación procesada (puede incluir rechazos)"),
            @ApiResponse(responseCode = "400", description = "Archivo inválido o inaccesible")
    })
    @PostMapping(value = "/importar-bioquimico",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportacionReporteDto> importarLote(
            @Parameter(description = "Archivo Excel (.xlsx) del laboratorio")
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "Periodo semestral (ej. 2026-1)")
            @RequestParam("periodo") String periodo) {

        ImportacionReporteDto reporte =
                bioquimicoService.importarLoteExcel(file, periodo);
        return ResponseEntity.ok(reporte);
    }

    // ===================================================================
    // POST /{idEvaluacion}/bioquimico/manual
    // ===================================================================

    @Operation(summary = "Registra el bioquímico de un colaborador de forma manual")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bioquímico registrado"),
            @ApiResponse(responseCode = "404", description = "Evaluación inexistente"),
            @ApiResponse(responseCode = "409", description = "Evaluación no PENDIENTE o ya tiene bioquímico")
    })
    @PostMapping("/{idEvaluacion}/bioquimico/manual")
    public ResponseEntity<BioquimicoResponse> registrarManual(
            @PathVariable Long idEvaluacion,
            @Valid @RequestBody BioquimicoManualRequest request) {

        BioquimicoResponse creada =
                bioquimicoService.registrarManual(idEvaluacion, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }


    @Operation(summary = "Actualiza el bioquímico existente (correcciones del laboratorio)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bioquímico actualizado"),
            @ApiResponse(responseCode = "404", description = "Evaluación o bioquímico inexistente"),
            @ApiResponse(responseCode = "409", description = "Evaluación anulada")
    })
    @PutMapping("/{idEvaluacion}/bioquimico/manual")
    public ResponseEntity<BioquimicoResponse> actualizarManual(
            @PathVariable Long idEvaluacion,
            @Valid @RequestBody BioquimicoManualRequest request) {

        return ResponseEntity.ok(
                bioquimicoService.actualizar(idEvaluacion, request));
    }
}