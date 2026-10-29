package upeu.edu.pe.Saludabemente.nutricional.evaluacion.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.Set;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.response.PeriodoResponse;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.dto.reporte.EvaluacionAgregado;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.dto.request.EvaluacionInitRequest;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.dto.request.ObservacionesRequest;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.dto.response.EvaluacionResponse;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.dto.response.EvaluacionResumen;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.enums.EstadoEvaluacion;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.service.EvaluacionService;

import java.util.List;
import java.util.Set;

@Tag(name = "Evaluación Nutricional",
        description = "Ciclo de vida de la cabecera de evaluación nutricional")
@RestController
@RequestMapping("/api/v1/evaluaciones-nutricionales")
@RequiredArgsConstructor
public class EvaluacionController {

    private final EvaluacionService evaluacionService;

    // ===================================================================
    // GET /api/v1/evaluaciones-nutricionales
    // ===================================================================

    private static final Set<String> CAMPOS_ORDENABLES = Set.of(
            "idEvaluacion",
            "fechaEvaluacion",
            "idPeriodo",
            "estadoEvaluacion",
            "idPersona"
    );

    @Operation(summary = "Lista evaluaciones con filtros opcionales y paginación")
    @ApiResponse(responseCode = "200", description = "Listado paginado de evaluaciones")
    @GetMapping
    public ResponseEntity<Page<EvaluacionResumen>> buscar(
            @Parameter(description = "Estado de la evaluación")
            @RequestParam(required = false) EstadoEvaluacion estado,

            @Parameter(description = "Periodo semestral (ej. 2026-1)")
            @RequestParam(required = false) String periodo,

            @Parameter(description = "ID de la persona")
            @RequestParam(required = false) Long idPersona,

            @PageableDefault(size = 20)
            Pageable pageable) {

        Pageable pageableSeguro = sanearPageable(pageable);
        return ResponseEntity.ok(
                evaluacionService.buscar(estado, periodo, idPersona, pageableSeguro));
    }

    /**
     * Ignora propiedades de ordenamiento que no sean campos reales de la entidad.
     * Swagger UI envía {@code sort=string} por defecto, y eso rompe Spring Data.
     * Si ningún campo es válido, aplica el orden por defecto: fechaEvaluacion DESC.
     */
    private Pageable sanearPageable(Pageable pageable) {
        Sort ordenSeguro = Sort.by(Sort.Direction.DESC, "fechaEvaluacion");

        if (pageable.getSort().isSorted()) {
            Sort filtrado = Sort.unsorted();
            for (Sort.Order order : pageable.getSort()) {
                if (CAMPOS_ORDENABLES.contains(order.getProperty())) {
                    filtrado = filtrado.and(
                            Sort.by(order.getDirection(), order.getProperty()));
                }
            }
            if (filtrado.isSorted()) {
                ordenSeguro = filtrado;
            }
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), ordenSeguro);
    }

    // ===================================================================
    // GET /api/v1/evaluaciones-nutricionales/agregados
    // ===================================================================

    @Operation(summary = "Métricas agregadas para el dashboard")
    @ApiResponse(responseCode = "200", description = "Totales y promedios")
    @GetMapping("/agregados")
    public ResponseEntity<EvaluacionAgregado> agregados(
            @Parameter(description = "Periodo a filtrar (opcional)")
            @RequestParam(required = false) String periodo) {

        return ResponseEntity.ok(evaluacionService.agregados(periodo));
    }

    // ===================================================================
    // GET /api/v1/evaluaciones-nutricionales/{id}
    // ===================================================================

    @Operation(summary = "Obtiene una evaluación completa por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evaluación encontrada"),
            @ApiResponse(responseCode = "404", description = "Evaluación inexistente")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EvaluacionResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(evaluacionService.obtener(id));
    }

    // ===================================================================
    // GET /api/v1/evaluaciones-nutricionales/persona/{idPersona}
    // ===================================================================

    @Operation(summary = "Historial cronológico de evaluaciones de una persona")
    @ApiResponse(responseCode = "200", description = "Listado ordenado por fecha descendente")
    @GetMapping("/persona/{idPersona}")
    public ResponseEntity<List<EvaluacionResumen>> historialPorPersona(
            @PathVariable Long idPersona) {

        return ResponseEntity.ok(evaluacionService.historialPorPersona(idPersona));
    }

    // ===================================================================
    // GET /api/v1/evaluaciones-nutricionales/incompletas
    // ===================================================================

    @Operation(summary = "Evaluaciones PENDIENTES de un periodo (seguimiento)")
    @ApiResponse(responseCode = "200", description = "Listado de cabeceras atascadas")
    @GetMapping("/incompletas")
    public ResponseEntity<List<EvaluacionResumen>> incompletas(
            @Parameter(description = "Periodo a filtrar", required = true)
            @RequestParam String periodo) {

        return ResponseEntity.ok(evaluacionService.incompletasPorPeriodo(periodo));
    }

    // ===================================================================
    // POST /api/v1/evaluaciones-nutricionales
    // ===================================================================

    @Operation(summary = "Inicia una evaluación capturando el snapshot clínico")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evaluación creada en PENDIENTE"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Persona no encontrada"),
            @ApiResponse(responseCode = "409", description = "Ya existe evaluación activa en el periodo")
    })
    @PostMapping
    public ResponseEntity<EvaluacionResponse> iniciar(
            @Valid @RequestBody EvaluacionInitRequest request) {

        EvaluacionResponse creada = evaluacionService.iniciar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    // ===================================================================
    // PATCH /api/v1/evaluaciones-nutricionales/{id}/observaciones
    // ===================================================================

    @Operation(summary = "Actualiza las observaciones médicas de una evaluación")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Observaciones actualizadas"),
            @ApiResponse(responseCode = "404", description = "Evaluación inexistente"),
            @ApiResponse(responseCode = "409", description = "Evaluación anulada")
    })
    @PatchMapping("/{id}/observaciones")
    public ResponseEntity<EvaluacionResponse> actualizarObservaciones(
            @PathVariable Long id,
            @Valid @RequestBody ObservacionesRequest request) {

        return ResponseEntity.ok(
                evaluacionService.actualizarObservaciones(id, request.getObservaciones()));
    }

    // ===================================================================
    // DELETE /api/v1/evaluaciones-nutricionales/{id}
    // ===================================================================

    @Operation(summary = "Anula una evaluación (baja lógica, no borra registros)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Evaluación anulada"),
            @ApiResponse(responseCode = "404", description = "Evaluación inexistente"),
            @ApiResponse(responseCode = "409", description = "Evaluación ya anulada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> anular(@PathVariable Long id) {
        evaluacionService.anular(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Lista los periodos donde se puede iniciar una evaluación",
            description = "Excluye los periodos CERRADOS. VIGENTE primero. "
                    + "Pensado para poblar dropdowns en el cliente.")
    @ApiResponse(responseCode = "200", description = "Lista curada de periodos disponibles")
    @GetMapping("/periodos-disponibles")
    public ResponseEntity<List<PeriodoResponse>> listarPeriodosDisponibles() {
        return ResponseEntity.ok(evaluacionService.listarPeriodosDisponibles());
    }



}