package upeu.edu.pe.Saludabemente.reportes;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialUsuarioApi;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/reportes/personales")
@RequiredArgsConstructor
public class ReportePersonalController {

    private final ReportePersonalService service;
    private final SocialUsuarioApi usuarios;

    @GetMapping("/asistencias")
    public ResponseEntity<byte[]> asistencias(@RequestParam String periodo) {
        Long idUsuario = usuarios.obtenerUsuarioActual().idUsuario();
        Long idPersona = usuarios.obtenerIdPersona(idUsuario);
        byte[] pdf = service.generarAsistenciasPdf(idPersona, periodo);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"asistencias-" + periodo + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/datos-personales")
    public ResponseEntity<byte[]> datosPersonales() {
        Long idUsuario = usuarios.obtenerUsuarioActual().idUsuario();
        Long idPersona = usuarios.obtenerIdPersona(idUsuario);
        byte[] pdf = service.generarDatosPersonalesPdf(idPersona);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"datos-personales.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/actividades")
    public ResponseEntity<byte[]> actividades(@RequestParam String desde, @RequestParam String hasta) {
        Long idUsuario = usuarios.obtenerUsuarioActual().idUsuario();
        Long idPersona = usuarios.obtenerIdPersona(idUsuario);
        LocalDate d = LocalDate.parse(desde);
        LocalDate h = LocalDate.parse(hasta);
        byte[] pdf = service.generarActividadesPdf(idPersona, d, h);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"actividades-" + desde + "-" + hasta + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/evaluaciones")
    public ResponseEntity<byte[]> evaluaciones() {
        Long idUsuario = usuarios.obtenerUsuarioActual().idUsuario();
        Long idPersona = usuarios.obtenerIdPersona(idUsuario);
        byte[] pdf = service.generarEvaluacionesPdf(idPersona);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"evaluaciones.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/alertas")
    public ResponseEntity<byte[]> alertas(@RequestParam String desde, @RequestParam String hasta) {
        Long idUsuario = usuarios.obtenerUsuarioActual().idUsuario();
        Long idPersona = usuarios.obtenerIdPersona(idUsuario);
        LocalDateTime d = LocalDateTime.parse(desde);
        LocalDateTime h = LocalDateTime.parse(hasta);
        byte[] pdf = service.generarAlertasPdf(idPersona, d, h);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"alertas-" + desde + "-" + hasta + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
