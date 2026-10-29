package upeu.edu.pe.Saludabemente.personal.credencial.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

/**
 * Genera imágenes QR usando la librería ZXing.
 * El resultado se devuelve codificado en Base64 para que el frontend
 * pueda renderizarlo con <img src="data:image/png;base64,...">.
 */
@Component
public class QrGeneratorUtil {

    private static final int DEFAULT_SIZE = 300;

    /**
     * Genera un PNG con el QR del texto dado y lo devuelve en Base64.
     */
    public String generarMatrizBase64(String contenido) {
        return generarMatrizBase64(contenido, DEFAULT_SIZE);
    }

    public String generarMatrizBase64(String contenido, int size) {
        if (contenido == null || contenido.isBlank()) {
            throw new IllegalArgumentException("El contenido del QR no puede ser vacío");
        }
        try {
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN, 1,
                    EncodeHintType.CHARACTER_SET, "UTF-8"
            );

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(contenido, BarcodeFormat.QR_CODE, size, size, hints);

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(matrix, "PNG", out);
                return Base64.getEncoder().encodeToString(out.toByteArray());
            }
        } catch (WriterException | IOException e) {
            throw new IllegalStateException("Error al generar el código QR", e);
        }
    }
}