package upeu.edu.pe.Saludabemente.reportes;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** Utilidad pura de conversion HTML a PDF para reportes personales. */
public final class PdfReporteUtil {

    private PdfReporteUtil() {
        throw new UnsupportedOperationException("Clase utilitaria, no instanciable");
    }

    public static byte[] generarPdfDesdeHtml(String html) {
        if (html == null || html.isBlank()) {
            throw new IllegalArgumentException("El HTML no puede estar vacio");
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
