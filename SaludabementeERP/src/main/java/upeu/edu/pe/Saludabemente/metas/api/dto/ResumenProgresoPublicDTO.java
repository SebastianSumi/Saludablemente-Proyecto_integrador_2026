package upeu.edu.pe.Saludabemente.metas.api.dto;

/**
 * Estado agregado de las metas de un colaborador.
 * Consumido por: perfil, recomendaciones-ia, exportacion.
 */
public record ResumenProgresoPublicDTO(
        Long idPersona,
        long metasActivas,
        long metasCompletadas,
        long metasDesviadas,
        long metasAbandonadas,
        double porcentajeCumplimientoGlobal,
        int puntosGamificacion
) {}