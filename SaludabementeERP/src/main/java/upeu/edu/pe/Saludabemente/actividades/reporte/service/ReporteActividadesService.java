package upeu.edu.pe.Saludabemente.actividades.reporte.service;

import upeu.edu.pe.Saludabemente.actividades.reporte.dto.response.ReporteActividadResponse;
import upeu.edu.pe.Saludabemente.actividades.reporte.dto.response.ReportePeriodoResponse;

import java.time.LocalDate;

public interface ReporteActividadesService {

    /** Reporte A: actividad + su configuracion completa. */
    ReporteActividadResponse reporteActividad(Long idActividad);

    /** Reporte B: agenda agrupada por periodo. */
    ReportePeriodoResponse reportePeriodo(LocalDate inicio, LocalDate fin, String periodo);
}