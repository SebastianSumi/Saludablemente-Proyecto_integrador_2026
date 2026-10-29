package upeu.edu.pe.Saludabemente.actividades.reporte.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.actividades.agenda.entity.Actividad;
import upeu.edu.pe.Saludabemente.actividades.agenda.repository.ActividadRepository;
import upeu.edu.pe.Saludabemente.actividades.configuracion.entity.ActividadMaterial;
import upeu.edu.pe.Saludabemente.actividades.configuracion.repository.ActividadMaterialRepository;
import upeu.edu.pe.Saludabemente.actividades.integracion.client.SeguridadClientService;
import upeu.edu.pe.Saludabemente.actividades.integracion.dto.CoordinadorSnapshotDto;
import upeu.edu.pe.Saludabemente.actividades.reporte.dto.response.ReporteActividadResponse;
import upeu.edu.pe.Saludabemente.actividades.reporte.dto.response.ReportePeriodoResponse;
import upeu.edu.pe.Saludabemente.actividades.reporte.repository.ReporteRepository;
import upeu.edu.pe.Saludabemente.actividades.reporte.service.ReporteActividadesService;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteActividadesActividadesServiceImpl implements ReporteActividadesService {

    private final ActividadRepository actividadRepository;
    private final ActividadMaterialRepository materialRepository;
    private final ReporteRepository reporteRepository;
    private final SeguridadClientService seguridadClient;

    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

    // ============================================================
    // Reporte A: Actividad + configuracion
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public ReporteActividadResponse reporteActividad(Long idActividad) {
        Actividad a = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada: " + idActividad));

        // Detalle: materiales
        List<String> materiales = materialRepository.findByIdActividad(idActividad)
                .stream()
                .map(ActividadMaterial::getDescripcion)
                .toList();

        // 👇 Cabecera: coordinador tolerante
        String nombreCoordinador = resolverNombreCoordinador(a.getIdUsuarioCoordinador());

        return new ReporteActividadResponse(
                a.getIdActividad(),
                a.getNombre(),
                a.getDescripcion(),
                a.getFechaActividad(),
                a.getHoraInicio(),
                a.getHoraFin(),
                a.getLugar(),
                a.getEstado(),
                a.getPuntajeSalud(),
                nombreCoordinador,
                materiales
        );
    }

    // ============================================================
    // Reporte B: Agenda por periodo
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public ReportePeriodoResponse reportePeriodo(LocalDate inicio, LocalDate fin, String periodo) {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Se requiere fechaInicio y fechaFin");
        }
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("fechaInicio no puede ser posterior a fechaFin");
        }

        List<Actividad> actividades = reporteRepository.findActividadesPorPeriodo(inicio, fin);

        long programadas = reporteRepository.contarPorEstado(inicio, fin, "PROGRAMADA");
        long enCurso     = reporteRepository.contarPorEstado(inicio, fin, "EN_CURSO");
        long finalizadas = reporteRepository.contarPorEstado(inicio, fin, "FINALIZADA");
        long canceladas  = reporteRepository.contarPorEstado(inicio, fin, "CANCELADA");

        List<ReportePeriodoResponse.ActividadItem> items = actividades.stream()
                .map(this::toItem)
                .toList();

        return new ReportePeriodoResponse(
                periodo != null ? periodo : (inicio + " / " + fin),
                inicio,
                fin,
                actividades.size(),
                (int) programadas,
                (int) enCurso,
                (int) finalizadas,
                (int) canceladas,
                items
        );
    }

    // ============================================================
    // helpers
    // ============================================================
    private ReportePeriodoResponse.ActividadItem toItem(Actividad a) {
        // 👇 Coordinador tolerante
        String nombreCoordinador = resolverNombreCoordinador(a.getIdUsuarioCoordinador());

        return new ReportePeriodoResponse.ActividadItem(
                a.getIdActividad(),
                a.getNombre(),
                a.getFechaActividad(),
                a.getHoraInicio().format(HHMM),
                a.getHoraFin().format(HHMM),
                a.getLugar(),
                a.getEstado(),
                a.getPuntajeSalud(),
                nombreCoordinador
        );
    }

    /**
     * Resuelve el nombre del coordinador sin romper si el usuario ya no existe.
     */
    private String resolverNombreCoordinador(Long idUsuarioCoordinador) {
        CoordinadorSnapshotDto coordinador =
                seguridadClient.obtenerCoordinadorOpcional(idUsuarioCoordinador);
        return coordinador != null
                ? coordinador.nombreCompleto()
                : "Sin asignar";
    }
}