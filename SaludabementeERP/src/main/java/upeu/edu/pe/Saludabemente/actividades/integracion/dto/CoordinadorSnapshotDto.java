package upeu.edu.pe.Saludabemente.actividades.integracion.dto;

/**
 * DTO interno de Actividades que representa un coordinador.
 * Es la traduccion de UsuarioSnapshotDto (Seguridad) a lo que
 * Actividades realmente necesita mostrar.
 */
public record CoordinadorSnapshotDto(
        Long idUsuario,
        String nombreCompleto,
        String email
) {}