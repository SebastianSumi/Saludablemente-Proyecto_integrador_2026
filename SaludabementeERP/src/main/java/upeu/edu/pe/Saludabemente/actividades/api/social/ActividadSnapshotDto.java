package upeu.edu.pe.Saludabemente.actividades.api.social;

import java.time.LocalDate;

/** Snapshot dedicado: no modifica el DTO existente consumido por Asistencias. */
public record ActividadSnapshotDto(Long idActividad, String nombre, String estado,
                                  LocalDate fechaActividad, String logoUrl) {}
