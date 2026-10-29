package upeu.edu.pe.Saludabemente.nutricional.util;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Utilidad pura de conversión HTML → PDF.
 * Cubre la parte PDF de la función 51 de PLANIFICACION.docx.
 *
 * <p>Sin estado, sin inyección. El HTML ya viene renderizado por Thymeleaf
 * desde el Service; aquí solo se transforma a bytes PDF.</p>
 */
public final class PdfReportUtil {

    private PdfReportUtil() {
        throw new UnsupportedOperationException("Clase utilitaria, no instanciable");
    }

    /**
     * Convierte un HTML completo (con {@code <html>}, {@code <head>}, {@code <body>})
     * a un arreglo de bytes PDF listo para descargar.
     */
    public static byte[] generarPdfDesdeHtml(String html) {
        if (html == null || html.isBlank()) {
            throw new IllegalArgumentException("El HTML no puede estar vacío");
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Error generando PDF: " + e.getMessage(), e);
        }
    }
}