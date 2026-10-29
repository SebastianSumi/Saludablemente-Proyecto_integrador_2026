package upeu.edu.pe.Saludabemente.seguridad.rol.dto.response;

/**
 * Representación pública de un rol del catálogo.
 */
public record RolResponse(
        Long id,
        String nombre,
        String descripcion
) {}