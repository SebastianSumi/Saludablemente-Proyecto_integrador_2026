package upeu.edu.pe.Saludabemente.social.perfil.dto;

import jakarta.validation.constraints.Size;

public record EditarPresentacionRequest(@Size(max = 300) String presentacion) {}
