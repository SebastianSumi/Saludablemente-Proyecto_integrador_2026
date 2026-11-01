package upeu.edu.pe.Saludabemente.personal.api;

/** Foto actual de una persona para lectura publica entre modulos, sin persistir copias. */
public record PersonaAvatarDTO(Long idPersona, String avatarUrl) {}
