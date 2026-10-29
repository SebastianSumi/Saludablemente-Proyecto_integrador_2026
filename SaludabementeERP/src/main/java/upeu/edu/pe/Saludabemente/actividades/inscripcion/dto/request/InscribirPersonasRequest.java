package upeu.edu.pe.Saludabemente.actividades.inscripcion.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record InscribirPersonasRequest(
        @NotNull List<Long> idsPersonas
) {}