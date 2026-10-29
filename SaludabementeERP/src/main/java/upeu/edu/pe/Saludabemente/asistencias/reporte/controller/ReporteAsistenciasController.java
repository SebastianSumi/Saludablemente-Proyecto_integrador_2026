package upeu.edu.pe.Saludabemente.asistencias.reporte.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.asistencias.reporte.dto.response.*;
import upeu.edu.pe.Saludabemente.asistencias.reporte.service.ReporteAsistenciasService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "Reportes", description = "Analítica y reportes motivacionales")
@RestController
@RequestMapping("/api/v1/asistencias/reportes")
@RequiredArgsConstructor
public class ReporteAsistenciasController {

    private final ReporteAsistenciasService reporteAsistenciasService;

    @Operation(summary = "Participación por actividad")
    @GetMapping("/participacion/{idActividad}")
    public ResponseEntity<ParticipacionActividadResponse> participacion(
            @PathVariable Long idActividad) {
        return ResponseEntity.ok(reporteAsistenciasService.participacionPorActividad(idActividad));
    }

    @Operation(summary = "Reporte por persona (periodo o rango)")
    @GetMapping("/persona/{idPersona}")
    public ResponseEntity<ReportePersonaResponse> porPersona(
            @PathVariable Long idPersona,
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(reporteAsistenciasService.reportePorPersona(idPersona, periodo, desde, hasta));
    }

    @Operation(summary = "Reporte por team (PERIODO o DIARIO)")
    @GetMapping("/team/{idTeam}")
    public ResponseEntity<ReporteTeamResponse> porTeam(
            @PathVariable Long idTeam,
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "PERIODO") String modo) {
        return ResponseEntity.ok(reporteAsistenciasService.reportePorTeam(idTeam, periodo, desde, hasta, modo));
    }

    @Operation(summary = "Ranking comparativo de teams")
    @GetMapping("/teams/ranking")
    public ResponseEntity<RankingTeamsResponse> rankingTeams(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "PERIODO") String modo) {
        return ResponseEntity.ok(reporteAsistenciasService.rankingTeams(periodo, desde, hasta, modo));
    }

    @Operation(summary = "Detección de ausentismo crónico")
    @GetMapping("/ausentismo")
    public ResponseEntity<Map<String, Object>> ausentismo(
            @RequestParam(defaultValue = "3") int minRacha) {
        List<Long> ids = reporteAsistenciasService.ausentismoCronico(minRacha);
        return ResponseEntity.ok(Map.of(
                "minRacha", minRacha,
                "total", ids.size(),
                "idsPersona", ids));
    }

    @Operation(summary = "Datos para gamificación (capa anticorrupción)")
    @GetMapping("/gamificacion/{idActividad}")
    public ResponseEntity<List<Long>> gamificacion(@PathVariable Long idActividad) {
        return ResponseEntity.ok(reporteAsistenciasService.asistentesParaGamificacion(idActividad));
    }
}