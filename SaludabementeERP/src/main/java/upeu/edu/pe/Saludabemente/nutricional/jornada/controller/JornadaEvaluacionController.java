package upeu.edu.pe.Saludabemente.nutricional.jornada.controller;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import upeu.edu.pe.Saludabemente.nutricional.jornada.dto.request.EstadoInscripcionRequest;
import upeu.edu.pe.Saludabemente.nutricional.jornada.dto.request.EstadoJornadaRequest;
import upeu.edu.pe.Saludabemente.nutricional.jornada.dto.request.JornadaEvaluacionRequest;
import upeu.edu.pe.Saludabemente.nutricional.jornada.dto.response.InscripcionJornadaResponse;
import upeu.edu.pe.Saludabemente.nutricional.jornada.dto.response.JornadaEvaluacionResponse;
import upeu.edu.pe.Saludabemente.nutricional.jornada.enums.EstadoJornada;
import upeu.edu.pe.Saludabemente.nutricional.jornada.service.JornadaEvaluacionService;

import java.util.List;
import java.util.Set;

@Tag(name = "Jornadas de Evaluación Nutricional",
        description = "Agenda de jornadas, cupos y turnos para evaluaciones nutricionales")
@RestController
@RequestMapping("/api/v1/jornadas-nutricionales")
@RequiredArgsConstructor
public class JornadaEvaluacionController {

    private static final Set<String> CAMPOS_ORDENABLES = Set.of(
            "idJornada",
            "nombre",
            "fecha",
            "hora",
            "estado",
            "idPeriodo",
            "fechaCreacion"
    );

    private final JornadaEvaluacionService jornadaService;

    @Operation(summary = "Lista jornadas con filtros opcionales y paginación")
    @ApiResponse(responseCode = "200", description = "Listado paginado de jornadas")
    @GetMapping
    public ResponseEntity<Page<JornadaEvaluacionResponse>> buscar(
            @Parameter(description = "Estado de la jornada")
            @RequestParam(required = false) EstadoJornada estado,

            @Parameter(description = "ID del periodo")
            @RequestParam(required = false) Long idPeriodo,

            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(
                jornadaService.buscar(estado, idPeriodo, sanearPageable(pageable)));
    }

    @Operation(summary = "Inscripciones de la persona autenticada")
    @ApiResponse(responseCode = "200", description = "Turnos propios")
    @GetMapping("/mis-inscripciones")
    public ResponseEntity<List<InscripcionJornadaResponse>> misInscripciones() {
        return ResponseEntity.ok(jornadaService.misInscripciones());
    }

    @Operation(summary = "Obtiene una jornada por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Jornada encontrada"),
            @ApiResponse(responseCode = "404", description = "Jornada inexistente")
    })
    @GetMapping("/{id}")
    public ResponseEntity<JornadaEvaluacionResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(jornadaService.obtener(id));
    }

    @Operation(summary = "Crea una jornada de evaluación nutricional")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Jornada creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Periodo inexistente"),
            @ApiResponse(responseCode = "409", description = "Periodo cerrado o sin permisos")
    })
    @PostMapping
    public ResponseEntity<JornadaEvaluacionResponse> crear(
            @Valid @RequestBody JornadaEvaluacionRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jornadaService.crear(request));
    }

    @Operation(summary = "Actualiza una jornada existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Jornada actualizada"),
            @ApiResponse(responseCode = "404", description = "Jornada inexistente"),
            @ApiResponse(responseCode = "409", description = "Regla de negocio incumplida")
    })
    @PutMapping("/{id}")
    public ResponseEntity<JornadaEvaluacionResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody JornadaEvaluacionRequest request) {

        return ResponseEntity.ok(jornadaService.actualizar(id, request));
    }

    @Operation(summary = "Cambia el estado de una jornada (abrir, cerrar o cancelar)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "404", description = "Jornada inexistente"),
            @ApiResponse(responseCode = "409", description = "Estado sin cambios o sin permisos")
    })
    @PatchMapping("/{id}/estado")
    public ResponseEntity<JornadaEvaluacionResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoJornadaRequest request) {

        return ResponseEntity.ok(
                jornadaService.cambiarEstado(id, request.estado()));
    }

    @Operation(summary = "Inscribe a la persona autenticada y asigna un turno")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inscripción creada con turno"),
            @ApiResponse(responseCode = "404", description = "Jornada inexistente"),
            @ApiResponse(responseCode = "409",
                    description = "Jornada cerrada, llena o inscripción duplicada")
    })
    @PostMapping("/{id}/inscripciones")
    public ResponseEntity<InscripcionJornadaResponse> inscribirme(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jornadaService.inscribirme(id));
    }

    @Operation(summary = "Lista las personas inscritas y sus turnos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inscritos ordenados por turno"),
            @ApiResponse(responseCode = "404", description = "Jornada inexistente"),
            @ApiResponse(responseCode = "409", description = "Sin permisos de administrador")
    })
    @GetMapping("/{id}/inscripciones")
    public ResponseEntity<List<InscripcionJornadaResponse>> listarInscritos(
            @PathVariable Long id) {
        return ResponseEntity.ok(jornadaService.listarInscritos(id));
    }

    @Operation(summary = "Cambia el estado de una inscripción (cancelar, reactivar, asistió)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "404", description = "Jornada o inscripción inexistente"),
            @ApiResponse(responseCode = "409", description = "Regla de negocio incumplida")
    })
    @PatchMapping("/{id}/inscripciones/{idInscripcion}/estado")
    public ResponseEntity<InscripcionJornadaResponse> cambiarEstadoInscripcion(
            @PathVariable Long id,
            @PathVariable Long idInscripcion,
            @Valid @RequestBody EstadoInscripcionRequest request) {

        return ResponseEntity.ok(jornadaService.cambiarEstadoInscripcion(
                id, idInscripcion, request.estado()));
    }

    private Pageable sanearPageable(Pageable pageable) {
        Sort ordenSeguro = Sort.by(Sort.Direction.ASC, "fecha")
                .and(Sort.by(Sort.Direction.ASC, "hora"));

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
}
