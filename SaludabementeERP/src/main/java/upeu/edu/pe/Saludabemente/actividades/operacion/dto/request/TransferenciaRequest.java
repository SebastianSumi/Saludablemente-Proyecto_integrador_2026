package upeu.edu.pe.Saludabemente.actividades.operacion.dto.request;

import jakarta.validation.constraints.NotNull;

public record TransferenciaRequest(
        @NotNull Long idNuevoCoordinador
) {}