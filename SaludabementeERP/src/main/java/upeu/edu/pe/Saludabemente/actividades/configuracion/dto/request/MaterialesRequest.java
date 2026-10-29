package upeu.edu.pe.Saludabemente.actividades.configuracion.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MaterialesRequest(
        @NotNull List<String> materiales
) {}