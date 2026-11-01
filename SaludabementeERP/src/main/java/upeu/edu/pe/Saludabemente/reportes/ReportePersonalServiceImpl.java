package upeu.edu.pe.Saludabemente.reportes;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import upeu.edu.pe.Saludabemente.actividades.api.ActividadApiService;
import upeu.edu.pe.Saludabemente.actividades.api.dto.ActividadSnapshotDto;
import upeu.edu.pe.Saludabemente.alertaclinica.api.AlertaReporteApiService;
import upeu.edu.pe.Saludabemente.alertaclinica.api.dto.AlertaReporteDTO;
import upeu.edu.pe.Saludabemente.asistencias.api.AsistenciaApiService;
import upeu.edu.pe.Saludabemente.asistencias.api.dto.HistorialAsistenciaDTO;
import upeu.edu.pe.Saludabemente.nutricional.api.EvaluacionApiService;
import upeu.edu.pe.Saludabemente.nutricional.api.EvaluacionResumenDTO;
import upeu.edu.pe.Saludabemente.personal.api.PersonalApiService;
import upeu.edu.pe.Saludabemente.personal.api.PersonalSnapshotDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportePersonalServiceImpl implements ReportePersonalService {

    private static final String PLANTILLA_ASISTENCIAS = "reportes/asistencias-personales";
    private static final String PLANTILLA_DATOS = "reportes/datos-personales";
    private static final String PLANTILLA_ACTIVIDADES = "reportes/actividades-personales";
    private static final String PLANTILLA_EVALUACIONES = "reportes/evaluaciones-personales";
    private static final String PLANTILLA_ALERTAS = "reportes/alertas-personales";

    private final AsistenciaApiService asistencias;
    private final ActividadApiService actividades;
    private final AlertaReporteApiService alertas;
    private final EvaluacionApiService evaluaciones;
    private final PersonalApiService personal;
    private final TemplateEngine templateEngine;

    @Override
    @Transactional(readOnly = true)
    public byte[] generarAsistenciasPdf(Long idPersona, String periodo) {
        HistorialAsistenciaDTO historial = asistencias.historialResumido(idPersona, periodo);

        Context ctx = new Context();
        ctx.setVariable("historial", historial);
        ctx.setVariable("periodo", periodo);

        String html = templateEngine.process(PLANTILLA_ASISTENCIAS, ctx);
        return PdfReporteUtil.generarPdfDesdeHtml(html);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarDatosPersonalesPdf(Long idPersona) {
        PersonalSnapshotDTO snapshot = personal.obtenerSnapshot(idPersona);

        Context ctx = new Context();
        ctx.setVariable("persona", snapshot);

        String html = templateEngine.process(PLANTILLA_DATOS, ctx);
        return PdfReporteUtil.generarPdfDesdeHtml(html);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarActividadesPdf(Long idPersona, LocalDate desde, LocalDate hasta) {
        List<ActividadSnapshotDto> lista = actividades.listarPorPersona(idPersona, desde, hasta);

        Context ctx = new Context();
        ctx.setVariable("actividades", lista);
        ctx.setVariable("desde", desde);
        ctx.setVariable("hasta", hasta);

        String html = templateEngine.process(PLANTILLA_ACTIVIDADES, ctx);
        return PdfReporteUtil.generarPdfDesdeHtml(html);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarEvaluacionesPdf(Long idPersona) {
        List<EvaluacionResumenDTO> lista = evaluaciones.historialPorPersona(idPersona);

        Context ctx = new Context();
        ctx.setVariable("evaluaciones", lista);

        String html = templateEngine.process(PLANTILLA_EVALUACIONES, ctx);
        return PdfReporteUtil.generarPdfDesdeHtml(html);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarAlertasPdf(Long idPersona, LocalDateTime desde, LocalDateTime hasta) {
        List<AlertaReporteDTO> lista = alertas.listarPorPersona(idPersona, desde, hasta);

        Context ctx = new Context();
        ctx.setVariable("alertas", lista);
        ctx.setVariable("desde", desde);
        ctx.setVariable("hasta", hasta);

        String html = templateEngine.process(PLANTILLA_ALERTAS, ctx);
        return PdfReporteUtil.generarPdfDesdeHtml(html);
    }
}
