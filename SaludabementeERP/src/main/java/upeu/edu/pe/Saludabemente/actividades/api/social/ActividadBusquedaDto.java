package upeu.edu.pe.Saludabemente.actividades.api.social;

import java.time.LocalDate;

public record ActividadBusquedaDto(Long idActividad, String nombre, String estado, LocalDate fechaActividad, String logoUrl) {}
