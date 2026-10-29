package upeu.edu.pe.Saludabemente.teams.api;

public record TeamSnapshotDTO(
        Long idTeam,
        String nombre,
        String descripcion,
        Boolean activo
) {
}
