package upeu.edu.pe.Saludabemente.notificaciones.notificacion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto.BandejaNotificacionDTO;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto.NotificacionResponse;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.service.notificacion.NotificacionService;

import java.util.List;

@Tag(name = "Notificaciones")
@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @Operation(summary = "Lista todas las notificaciones registradas")
    @GetMapping
    public ResponseEntity<List<NotificacionResponse>> listar() {
        return ResponseEntity.ok(notificacionService.listar());
    }

    @Operation(summary = "Consulta una notificación por id")
    @GetMapping("/{id}")
    public ResponseEntity<NotificacionResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(notificacionService.obtener(id));
    }

    @Operation(summary = "Lista la bandeja del colaborador (leídas y no leídas, excluye archivadas)")
    @GetMapping("/mis-alertas")
    public ResponseEntity<List<BandejaNotificacionDTO>> misAlertas(@RequestParam Long idPersona) {
        return ResponseEntity.ok(notificacionService.listarBandeja(idPersona));
    }

    @Operation(summary = "Cuenta las notificaciones no leídas de un colaborador")
    @GetMapping("/contador-no-leidas")
    public ResponseEntity<Long> contadorNoLeidas(@RequestParam Long idPersona) {
        return ResponseEntity.ok(notificacionService.contarNoLeidas(idPersona));
    }

    @Operation(summary = "Marca una notificación como leída")
    @PutMapping("/{id}/marcar-leida")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarLeida(@PathVariable Long id) {
        notificacionService.marcarLeida(id);
    }

    @Operation(summary = "Archiva una notificación (deja de aparecer en la bandeja)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archivar(@PathVariable Long id) {
        notificacionService.archivar(id);
    }
}