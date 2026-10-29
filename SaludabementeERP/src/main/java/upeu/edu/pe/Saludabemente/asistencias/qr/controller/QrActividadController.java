package upeu.edu.pe.Saludabemente.asistencias.qr.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import upeu.edu.pe.Saludabemente.asistencias.qr.dto.response.QrActividadResponse;
import upeu.edu.pe.Saludabemente.asistencias.qr.service.QrActividadService;
import upeu.edu.pe.Saludabemente.asistencias.qr.service.QrStreamService;

@Tag(name = "QR Actividad", description = "Generación y rotación del QR general")
@RestController
@RequestMapping("/api/v1/actividades/{idActividad}/qr")
@RequiredArgsConstructor
public class QrActividadController {

    private final QrActividadService qrActividadService;
    private final QrStreamService qrStreamService;

    @Operation(summary = "Forzar rotación del QR")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "QR rotado"),
            @ApiResponse(responseCode = "404", description = "Actividad no encontrada"),
            @ApiResponse(responseCode = "409", description = "Actividad no permite QR")
    })
    @PostMapping("/rotar")
    public ResponseEntity<QrActividadResponse> rotar(@PathVariable Long idActividad) {
        QrActividadResponse qr = qrActividadService.rotar(idActividad);
        qrStreamService.emitir(idActividad, qr);
        return ResponseEntity.ok(qr);
    }

    @Operation(summary = "Obtener el QR vigente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "QR devuelto"),
            @ApiResponse(responseCode = "404", description = "No hay QR vigente")
    })
    @GetMapping("/actual")
    public ResponseEntity<QrActividadResponse> obtenerActual(@PathVariable Long idActividad) {
        return ResponseEntity.ok(qrActividadService.obtenerActual(idActividad));
    }

    @Operation(summary = "Stream SSE con rotación automática del QR")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable Long idActividad) {
        return qrStreamService.suscribir(idActividad);
    }
}