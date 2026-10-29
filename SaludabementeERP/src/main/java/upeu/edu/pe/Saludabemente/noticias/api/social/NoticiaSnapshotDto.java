package upeu.edu.pe.Saludabemente.noticias.api.social;

import java.time.LocalDateTime;

/** resumen/equipo/url son explícitos y opcionales; nunca se deducen de segmentación. */
public record NoticiaSnapshotDto(Long idNoticia, String titulo, String resumen,
        String imagenPortadaUrl, LocalDateTime fechaPublicacion, String estado,
        boolean oficial, String urlCompleta, Long idUsuarioAutor, Long idEquipoAutor) {}
