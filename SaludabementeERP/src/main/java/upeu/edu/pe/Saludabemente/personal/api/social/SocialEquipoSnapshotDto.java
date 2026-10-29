package upeu.edu.pe.Saludabemente.personal.api.social;

public record SocialEquipoSnapshotDto(Long idEquipo, String nombre, String descripcion,
        String logoUrl, boolean activo, long totalMiembros) {}
