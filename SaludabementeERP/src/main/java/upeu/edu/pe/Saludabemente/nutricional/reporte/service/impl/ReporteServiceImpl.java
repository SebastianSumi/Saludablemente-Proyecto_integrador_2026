package upeu.edu.pe.Saludabemente.nutricional.reporte.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.dto.response.EvaluacionResponse;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.repository.EvaluacionRepository;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.service.EvaluacionService;
import upeu.edu.pe.Saludabemente.nutricional.reporte.service.ReporteService;
import upeu.edu.pe.Saludabemente.nutricional.util.PdfReportUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private static final String PLANTILLA_FICHA = "nutricional/ficha-clinica";

    private final EvaluacionRepository evaluacionRepository;
    private final EvaluacionService evaluacionService;
    private final TemplateEngine templateEngine;

    // ===================================================================
    // Excel consolidado
    // ===================================================================

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarConsolidadoSemestral(String periodo) {
        List<Object[]> filas = evaluacionRepository.findResumenConsolidadoByPeriodo(periodo);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Consolidado " + periodo);
            escribirEncabezado(workbook, sheet);
            escribirFilas(sheet, filas);
            ajustarAnchos(sheet);

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Error generando Excel consolidado: " + e.getMessage(), e);
        }
    }

    private void escribirEncabezado(Workbook workbook, Sheet sheet) {
        String[] columnas = {
                "N°", "DNI", "Nombres", "Área", "Periodo", "Fecha",
                "Estado", "IMC", "Dx IMC", "Dx Bioquímico"
        };

        CellStyle estilo = workbook.createCellStyle();
        Font negrita = workbook.createFont();
        negrita.setBold(true);
        estilo.setFont(negrita);

        Row header = sheet.createRow(0);
        for (int i = 0; i < columnas.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(estilo);
        }
    }

    private void escribirFilas(Sheet sheet, List<Object[]> filas) {
        int fila = 1;
        for (Object[] f : filas) {
            // Orden del SELECT en EvaluacionRepository.findResumenConsolidadoByPeriodo:
            //   [0] idEvaluacion, [1] idPersona, [2] documentoIdentidad,
            //   [3] nombresCompletos, [4] areaTrabajo, [5] idPeriodo,
            //   [6] fechaEvaluacion, [7] estado, [8] imc, [9] dxImc, [10] dxBioquimico
            Row row = sheet.createRow(fila);
            row.createCell(0).setCellValue(fila);
            row.createCell(1).setCellValue(str(f[2]));
            row.createCell(2).setCellValue(str(f[3]));
            row.createCell(3).setCellValue(str(f[4]));
            row.createCell(4).setCellValue(str(f[5]));
            row.createCell(5).setCellValue(str(f[6]));
            row.createCell(6).setCellValue(str(f[7]));
            row.createCell(7).setCellValue(num(f[8]));
            row.createCell(8).setCellValue(str(f[9]));
            row.createCell(9).setCellValue(str(f[10]));
            fila++;
        }
    }

    private void ajustarAnchos(Sheet sheet) {
        for (int i = 0; i < 10; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // ===================================================================
    // Ficha clínica PDF
    // ===================================================================

    @Override
    @Transactional(readOnly = true)
    public byte[] generarFichaClinicaPdf(Long idEvaluacion) {
        EvaluacionResponse evaluacion = evaluacionService.obtener(idEvaluacion);

        Context ctx = new Context();
        ctx.setVariable("evaluacion", evaluacion);

        String html = templateEngine.process(PLANTILLA_FICHA, ctx);
        return PdfReportUtil.generarPdfDesdeHtml(html);
    }

    // ===================================================================
    // Helpers
    // ===================================================================

    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }

    private static double num(Object o) {
        return o == null ? 0.0 : ((Number) o).doubleValue();
    }
}