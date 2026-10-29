package upeu.edu.pe.socialcontracts;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.shared.storage.AlmacenamientoImagenService;
import upeu.edu.pe.Saludabemente.shared.storage.social.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlmacenamientoContratoTest {
    private final AlmacenamientoImagenService almacenamiento = mock(AlmacenamientoImagenService.class);
    private final SocialComentarioStorageService api = new SocialComentarioStorageService(almacenamiento);
    private final String base = "https://minio.example.invalid/bucket/social/comentarios/601/";

    @ParameterizedTest
    @ValueSource(strings = {"image/jpeg", "image/png", "image/webp"})
    void admiteTresMime(String mime) {
        assertDoesNotThrow(() -> api.validar(new MockMultipartFile("archivo", "imagen", mime, new byte[]{1})));
    }

    @Test void rechazaVacioMimeAusenteGifYExtensionMaliciosa() {
        assertThrows(BusinessRuleException.class, () -> api.validar(null));
        assertThrows(BusinessRuleException.class, () -> api.validar(new MockMultipartFile("archivo", new byte[0])));
        assertThrows(BusinessRuleException.class, () -> api.validar(new MockMultipartFile("archivo", "a.png", null, new byte[]{1})));
        assertThrows(BusinessRuleException.class, () -> api.validar(new MockMultipartFile("archivo", "a.gif", "image/gif", new byte[]{1})));
        assertThrows(BusinessRuleException.class, () -> api.validar(new MockMultipartFile("archivo", "a.png/evil", "image/png", new byte[]{1})));
        verifyNoInteractions(almacenamiento);
    }

    @Test void limiteExactoDeCincoMiBYUnByteDeMas() {
        MultipartFile archivo = mock(MultipartFile.class);
        when(archivo.getContentType()).thenReturn("image/png");
        when(archivo.getOriginalFilename()).thenReturn("a.png");
        when(archivo.getSize()).thenReturn(AdjuntoSocialStorageApi.MAX_BYTES);
        assertDoesNotThrow(() -> api.validar(archivo));
        when(archivo.getSize()).thenReturn(AdjuntoSocialStorageApi.MAX_BYTES + 1);
        assertThrows(BusinessRuleException.class, () -> api.validar(archivo));
    }

    @Test void guardarSeparaClaveUrlYUsaPrefijoLocal() {
        var archivo = new MockMultipartFile("archivo", "a.png", "image/png", new byte[]{1, 2});
        String key = "social/comentarios/601/abc.png";
        when(almacenamiento.guardar(archivo, "social/comentarios/601/", AdjuntoSocialStorageApi.MAX_BYTES)).thenReturn(key);
        when(almacenamiento.construirUrlPublica(key)).thenReturn(base + "abc.png");
        AdjuntoSocialDto resultado = api.guardar(601L, archivo);
        assertEquals(key, resultado.objectKey()); assertEquals(base + "abc.png", resultado.url());
        assertEquals(2, resultado.sizeBytes());
        assertThrows(BusinessRuleException.class, () -> api.guardar(0L, archivo));
    }

    @Test void cuatroReferenciasValidasYRechazaQuintaODuplicadas() {
        when(almacenamiento.construirUrlPublica("social/comentarios/601/")).thenReturn(base);
        List<String> cuatro = List.of(base + "a.png", base + "b.webp", base + "c.jpg", base + "d.jpeg");
        assertDoesNotThrow(() -> api.validarReferencias(601L, cuatro));
        assertThrows(BusinessRuleException.class, () -> api.validarReferencias(601L, List.of(base + "a.png", base + "a.png")));
        assertThrows(BusinessRuleException.class, () -> api.validarReferencias(601L,
                List.of(base + "a.png", base + "b.png", base + "c.png", base + "d.png", base + "e.png")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"../evil.png", "%2e%2e/evil.png", "a.png?x=1", "a.png#f", "a\\evil.png", "a.png/evil", "a%2f.png"})
    void rechazaReferenciasAmbiguas(String sufijo) {
        when(almacenamiento.construirUrlPublica("social/comentarios/601/")).thenReturn(base);
        assertThrows(BusinessRuleException.class, () -> api.validarReferencias(601L, List.of(base + sufijo)));
    }

    @Test void rechazaAutoridadOPostDistintosYNoAceptaClaveComoUrl() {
        when(almacenamiento.construirUrlPublica("social/comentarios/601/")).thenReturn(base);
        for (String url : List.of(base.replace("/601/", "/602/") + "a.png",
                base.replace("minio.example.invalid", "otro.example.invalid") + "a.png",
                "social/comentarios/601/a.png"))
            assertThrows(BusinessRuleException.class, () -> api.validarReferencias(601L, List.of(url)));
    }
}
