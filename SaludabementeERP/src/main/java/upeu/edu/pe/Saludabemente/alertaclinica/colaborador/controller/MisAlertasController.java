package upeu.edu.pe.Saludabemente.alertaclinica.colaborador.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto.AlertaClinicaResponse;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.*;
import upeu.edu.pe.Saludabemente.alertaclinica.colaborador.service.MisAlertasService;
import java.util.List;

@RestController
@RequestMapping("/api/v1/mis-alertas")
@Tag(name = "Mis alertas clinicas (solo lectura)")
@RequiredArgsConstructor
public class MisAlertasController {
    private final MisAlertasService service;

    @GetMapping
    @Operation(summary = "Consulta las alertas y etiquetas de la persona autenticada",
            description = "La persona se obtiene de la sesion. No recibe personaId del cliente.")
    public List<AlertaClinicaResponse> listar(@RequestParam(required = false) EstadoAlerta estado,
            @RequestParam(required = false) SeveridadAlerta severidad,
            @RequestParam(required = false) TipoEvaluacionOrigen tipoOrigen) {
        return service.listar(estado, severidad, tipoOrigen);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta una alerta propia con indicadores y etiquetas",
            description = "Devuelve 404 si no pertenece a la persona autenticada o no existe. Solo lectura.")
    public AlertaClinicaResponse obtener(@PathVariable Long id) { return service.obtener(id); }
}
