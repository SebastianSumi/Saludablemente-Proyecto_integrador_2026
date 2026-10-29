package upeu.edu.pe.Saludabemente.personal.api.social;

/** Identidad visual resuelta en vivo; no incluye correo ni roles. */
public record SocialLiderEquipoDto(Long idUsuario, String username, String nombreCompleto,
                                  String fotoUrl, boolean activo) {}
