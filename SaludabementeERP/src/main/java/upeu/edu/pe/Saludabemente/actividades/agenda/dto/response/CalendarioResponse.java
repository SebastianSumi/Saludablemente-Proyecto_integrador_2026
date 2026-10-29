package upeu.edu.pe.Saludabemente.actividades.agenda.dto.response;

import java.util.List;

public record CalendarioResponse(
        int mes,
        int anio,
        List<ActividadResponse> actividades
) {}