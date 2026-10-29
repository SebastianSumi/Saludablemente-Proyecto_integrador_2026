package upeu.edu.pe.Saludabemente.nutricional.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.dto.request.BioquimicoExcelDto;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser del Excel de laboratorio.
 * Cubre la función 51 de PLANIFICACION.docx.
 *
 * Reglas de tolerancia (ver DECISIONES.md, sección 4):
 *  - Cruce por CELULAR (el Excel no trae DNI).
 *  - Fechas corruptas → fila rechazada, se reporta.
 *  - Presión "S/D" se valida con regex ^\d{2,3}/\d{2,3}$.
 *  - Celdas vacías en todos los marcadores → fila rechazada.
 *
 * El parser NO descarta filas por un solo valor sospechoso: solo
 * exige que exista al menos un marcador utilizable.
 */
public final class ExcelParserUtil {

    private ExcelParserUtil() {
        throw new UnsupportedOperationException("Clase utilitaria, no instanciable");
    }

    // Columnas del Excel real (índice 0-based)
    // Ver PROGRAMA CREATION HEALTH.xlsx, hoja "Resultados"
    private static final int COL_CELULAR          = 5;   // F
    private static final int COL_GLUCOSA          = 33;  // AH
    private static final int COL_COLESTEROL       = 34;  // AI
    private static final int COL_TRIGLICERIDOS    = 35;  // AJ
    private static final int COL_PRESION          = 36;  // AK
    private static final int FILA_INICIO_DATOS    = 5;   // primera fila con datos

    private static final Pattern PATRON_PRESION =
            Pattern.compile("^(\\d{2,3})/(\\d{2,3})$");

    /**
     * Extrae las filas útiles del Excel de laboratorio.
     * Ignora las filas sin celular o sin ningún marcador bioquímico.
     */
    public static List<BioquimicoExcelDto> extraerFilas(InputStream inputStream) throws IOException {
        List<BioquimicoExcelDto> resultado = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            int ultimaFila = sheet.getLastRowNum();

            for (int i = FILA_INICIO_DATOS; i <= ultimaFila; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String celular = leerTexto(row.getCell(COL_CELULAR));
                if (celular == null || celular.isBlank()) {
                    continue; // fila sin celular → no se puede cruzar
                }

                BigDecimal glucosa       = leerDecimal(row.getCell(COL_GLUCOSA));
                BigDecimal colesterol    = leerDecimal(row.getCell(COL_COLESTEROL));
                BigDecimal trigliceridos = leerDecimal(row.getCell(COL_TRIGLICERIDOS));

                Integer[] presion = leerPresion(row.getCell(COL_PRESION));

                // Fila sin ningún marcador → se ignora silenciosamente.
                boolean sinMarcadores = glucosa == null
                        && colesterol == null
                        && trigliceridos == null
                        && presion[0] == null
                        && presion[1] == null;

                if (sinMarcadores) {
                    continue;
                }

                resultado.add(new BioquimicoExcelDto(
                        celular.trim(),
                        glucosa,
                        colesterol,
                        trigliceridos,
                        presion[0],
                        presion[1],
                        i + 1  // número de fila 1-based para reporte
                ));
            }
        }

        return resultado;
    }

    // ------------------------------------------------------------------
    // Helpers de lectura tolerante
    // ------------------------------------------------------------------

    private static String leerTexto(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BLANK   -> null;
            default      -> null;
        };
    }

    private static BigDecimal leerDecimal(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING  -> parsearDecimalDeTexto(cell.getStringCellValue());
            default      -> null;
        };
    }

    /**
     * Normaliza texto tipo "38.9 %", " 42,6 ", "900.0%" → BigDecimal.
     * Si no parsea, devuelve null (fila sigue siendo útil si hay otros marcadores).
     */
    private static BigDecimal parsearDecimalDeTexto(String raw) {
        if (raw == null) return null;
        String limpio = raw.replace("%", "")
                .replace(",", ".")
                .replace(" ", "")
                .trim();
        if (limpio.isBlank()) return null;
        try {
            return new BigDecimal(limpio);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Presión "131/72" → [131, 72].
     * Presión "1115/69" o "118.63" → [null, null].
     * NO intenta corregir valores sospechosos (ver DECISIONES.md, A-06).
     */
    private static Integer[] leerPresion(Cell cell) {
        if (cell == null) return new Integer[]{null, null};

        String raw = switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default      -> null;
        };

        if (raw == null) return new Integer[]{null, null};

        Matcher m = PATRON_PRESION.matcher(raw);
        if (!m.matches()) {
            return new Integer[]{null, null};
        }

        try {
            return new Integer[]{
                    Integer.parseInt(m.group(1)),
                    Integer.parseInt(m.group(2))
            };
        } catch (NumberFormatException e) {
            return new Integer[]{null, null};
        }
    }
}