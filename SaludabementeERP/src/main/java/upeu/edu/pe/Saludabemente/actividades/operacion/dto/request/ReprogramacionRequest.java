package upeu.edu.pe.Saludabemente.actividades.operacion.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReprogramacionRequest(
        @NotNull LocalDate nuevaFecha,
        @NotNull LocalTime nuevaHoraInicio,
        @NotNull LocalTime nuevaHoraFin
) {}