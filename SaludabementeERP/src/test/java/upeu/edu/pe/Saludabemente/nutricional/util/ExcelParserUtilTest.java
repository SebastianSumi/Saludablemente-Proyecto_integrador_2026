package upeu.edu.pe.Saludabemente.nutricional.util;

import org.junit.jupiter.api.Test;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.dto.request.BioquimicoExcelDto;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelParserUtilTest {

    @Test
    void parseaExcelDePruebaSinFallar() throws Exception {
        byte[] bytes;
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("Resultados");
            // Fill headers until row index 4
            for (int r = 0; r < 5; r++) {
                sheet.createRow(r);
            }
            // Data row index 5
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(5);
            row.createCell(5).setCellValue("987654321"); // COL_CELULAR = 5
            row.createCell(33).setCellValue(95.5);      // COL_GLUCOSA = 33
            row.createCell(34).setCellValue(180.0);     // COL_COLESTEROL = 34
            row.createCell(35).setCellValue(140.0);     // COL_TRIGLICERIDOS = 35
            row.createCell(36).setCellValue("120/80");  // COL_PRESION = 36

            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            wb.write(out);
            bytes = out.toByteArray();
        }

        try (InputStream in = new java.io.ByteArrayInputStream(bytes)) {
            List<BioquimicoExcelDto> filas = ExcelParserUtil.extraerFilas(in);

            assertThat(filas).isNotEmpty();
            filas.forEach(f -> assertThat(f.celular()).isNotBlank());
            assertThat(filas.get(0).celular()).isEqualTo("987654321");
            assertThat(filas.get(0).glucosa()).isEqualTo(new java.math.BigDecimal("95.5"));
            assertThat(filas.get(0).presionSistolica()).isEqualTo(120);
            assertThat(filas.get(0).presionDiastolica()).isEqualTo(80);
        }
    }
}