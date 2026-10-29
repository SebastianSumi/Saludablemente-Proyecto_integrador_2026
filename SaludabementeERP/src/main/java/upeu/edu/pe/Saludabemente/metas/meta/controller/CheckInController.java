package upeu.edu.pe.Saludabemente.metas.meta.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.metas.meta.dto.CheckInRequestDTO;
import upeu.edu.pe.Saludabemente.metas.meta.dto.ProgresoMetaResponseDTO;
import upeu.edu.pe.Saludabemente.metas.meta.service.AuditoriaMetasService;
import upeu.edu.pe.Saludabemente.metas.meta.service.CheckInService;
import upeu.edu.pe.Saludabemente.metas.meta.service.NotificacionMetasService;
import upeu.edu.pe.Saludabemente.metas.shared.enums.EstadoMeta;
import upeu.edu.pe.Saludabemente.seguridad.api.metas.MetasUsuarioApi;
import upeu.edu.pe.Saludabemente.seguridad.api.metas.UsuarioActualDTO;

import java.util.List;

@Slf4j
@Tag(name = "Check-ins")
@RestController
@RequestMapping("/api/v1/metas/checkins")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;
    private final NotificacionMetasService notificacionMetas;
    private final AuditoriaMetasService auditoriaMetas;
    private final MetasUsuarioApi seguridadApi;

    @Operation(summary = "Registra un avance sobre una meta")
    @PostMapping
    public ResponseEntity<ProgresoMetaResponseDTO> registrar(
            @Valid @RequestBody CheckInRequestDTO dto) {

        if (!seguridadApi.tieneAlgunRol("Colaborador", "Administrador")) {
            throw new BusinessRuleException("No autorizado para registrar check-ins");
        }
        UsuarioActualDTO usuario = seguridadApi.obtenerUsuarioActual();

        ProgresoMetaResponseDTO resultado = checkInService.registrar(dto, usuario.email());

        try {
            auditoriaMetas.registrarCheckIn(
                    usuario.idUsuario(),
                    resultado.idMeta(),
                    null,
                    resultado.modalidad().name(),
                    resultado.valorActual() == null ? null : resultado.valorActual().toString());
        } catch (RuntimeException ex) {
            log.warn("Fallo al auditar check-in de meta {}: {}",
                    resultado.idMeta(), ex.getMessage());
        }

        if (resultado.estado() == EstadoMeta.COMPLETADA) {
            try {
                notificacionMetas.notificarMetaCompletada(
                        resultado.idPersona(),
                        resultado.idMeta(),
                        resultado.descripcion());
            } catch (RuntimeException ex) {
                log.warn("Fallo al notificar meta completada {}: {}",
                        resultado.idMeta(), ex.getMessage());
            }
        }

        return ResponseEntity.ok(resultado);
    }

    @Operation(summary = "Historial de check-ins de una meta")
    @GetMapping("/meta/{idMeta}")
    public ResponseEntity<List<ProgresoMetaResponseDTO>> historial(@PathVariable Long idMeta) {

        if (!seguridadApi.tieneAlgunRol("Colaborador", "Administrador", "Evaluador")) {
            throw new BusinessRuleException("No autorizado para consultar historial");
        }
        return ResponseEntity.ok(checkInService.historial(idMeta));
    }
}