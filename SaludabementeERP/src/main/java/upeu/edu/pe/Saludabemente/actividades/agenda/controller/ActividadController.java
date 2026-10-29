package upeu.edu.pe.Saludabemente.actividades.agenda.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.actividades.agenda.dto.request.ActividadRequest;
import upeu.edu.pe.Saludabemente.actividades.agenda.dto.request.SedeRequest;
import upeu.edu.pe.Saludabemente.actividades.agenda.dto.response.ActividadDetalleResponse;
import upeu.edu.pe.Saludabemente.actividades.agenda.dto.response.ActividadResponse;
import upeu.edu.pe.Saludabemente.actividades.agenda.dto.response.CalendarioResponse;
import upeu.edu.pe.Saludabemente.actividades.agenda.service.AgendaService;
import upeu.edu.pe.Saludabemente.actividades.inscripcion.dto.response.InscritoDetalleResponse;
import upeu.edu.pe.Saludabemente.actividades.inscripcion.service.InscripcionService;

import java.util.List;

@Tag(name = "Actividades - Agenda")
@RestController
@RequestMapping("/api/v1/agenda")
@RequiredArgsConstructor
public class ActividadController {

    private final AgendaService agendaService;
    private final InscripcionService inscripcionService;

    @Operation(summary = "Listar actividades para el calendario mensual")
    @GetMapping("/calendario")
    public ResponseEntity<CalendarioResponse> listarParaCalendario(
            @RequestParam int mes,
            @RequestParam int anio) {
        return ResponseEntity.ok(agendaService.listarParaCalendario(mes, anio));
    }

    @Operation(summary = "Actividades activas del dia de hoy")
    @GetMapping("/hoy")
    public ResponseEntity<List<ActividadResponse>> obtenerActivasDelDia() {
        return ResponseEntity.ok(agendaService.obtenerActivasDelDia());
    }

    @Operation(summary = "Crear actividad individual")
    @PostMapping("/actividades")
    public ResponseEntity<ActividadResponse> crear(@Valid @RequestBody ActividadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(agendaService.crearActividadIndividual(request));
    }

    @Operation(summary = "Obtener actividad por ID")
    @GetMapping("/actividades/{id}")
    public ResponseEntity<ActividadDetalleResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(agendaService.obtenerPorId(id));
    }

    @Operation(summary = "Listar todas las actividades")
    @GetMapping("/actividades")
    public ResponseEntity<List<ActividadResponse>> listar() {
        return ResponseEntity.ok(agendaService.listarTodas());
    }

    @Operation(summary = "Detalle de las personas inscritas en una actividad")
    @GetMapping("/actividades/{id}/inscripciones")
    public ResponseEntity<List<InscritoDetalleResponse>> listarInscritosDetalle(
            @PathVariable Long id) {
        return ResponseEntity.ok(inscripcionService.listarInscritosDetalle(id));
    }

    @Operation(summary = "Actualizar sede y radio de geocerca (solo PROGRAMADA o EN_CURSO)")
    @PutMapping("/actividades/{id}/sede")
    public ResponseEntity<ActividadResponse> actualizarSede(
            @PathVariable Long id,
            @Valid @RequestBody SedeRequest request) {
        return ResponseEntity.ok(agendaService.actualizarSede(id, request));
    }
}