package upeu.edu.pe.Saludabemente.actividades.integracion.dto;

/**
 * DTO interno de Actividades con los datos de un usuario (creador o coordinador).
 * Es la traduccion de UsuarioSnapshotDto (Seguridad) a lo que
 * Actividades realmente necesita mostrar.
 */
public record UsuarioActividadDto(
        Long idUsuario,
        String nombreCompleto,
        String email,
        String rol
) {}
