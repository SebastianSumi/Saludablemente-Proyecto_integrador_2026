package upeu.edu.pe.Saludabemente.teams.api;

import java.util.List;

/** Puerto de identidad y membresía implementado por Seguridad, sin dependencias inversas. */
public interface TeamAccesoApi {
    Long usuarioActual();
    boolean esAdministrador();
    boolean esMiembroActivo(Long idUsuario, Long idEquipo);
    Long equipoActual();
    List<CandidatoLider> candidatos(Long idEquipo);
    record CandidatoLider(Long idUsuario, String nombre) {}
}
