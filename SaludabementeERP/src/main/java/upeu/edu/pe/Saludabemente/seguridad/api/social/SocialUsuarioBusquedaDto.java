package upeu.edu.pe.Saludabemente.seguridad.api.social;

/**
 * Resultado resumido para el autocompletado de menciones {@code @usuario}.
 *
 * @param idUsuario     PK interno en Seguridad
 * @param username      identificador corto para el {@code @}
 * @param nombreCompleto nombre para mostrar
 * @param fotoUrl       URL de la foto de perfil, puede ser {@code null}
 */
public record SocialUsuarioBusquedaDto(
        Long idUsuario,
        String username,
        String nombreCompleto,
        String fotoUrl
) {}