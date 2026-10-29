package upeu.edu.pe.Saludabemente.social.api.dto;

import java.time.LocalDateTime;

/** Payload de sincronización definido en el Documento Maestro v1.0, sección 10.2. */
public record NoticiaSocialDto(
        Long idNoticia,
        String titulo,
        String resumen,
        String imagenPortadaUrl,
        LocalDateTime fechaPublicacion,
        String estado,
        boolean oficial,
        Long idUsuarioAutor,
        Long idEquipoAutor,
        String urlCompleta
) {}
