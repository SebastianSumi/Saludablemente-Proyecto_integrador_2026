package upeu.edu.pe.Saludabemente.personal.api.social;

import java.util.List;

/** Contrato del maestro, compuesto por Seguridad usando APIs de Personal y Teams. */
public interface SocialEquipoApi {
    /** Ausente: ResourceNotFoundException. logoUrl referencia la imagen pública de Teams. */
    SocialEquipoSnapshotDto obtenerPorId(Long idEquipo);
    /** Máximo 100 IDs; omite equipos ausentes, sin orden de entrada garantizado. */
    List<SocialEquipoSnapshotDto> obtenerPorIds(List<Long> ids);
    /** Perfil visual y líder resuelto en vivo. Ausente: ResourceNotFoundException. */
    SocialEquipoPerfilDto obtenerPerfil(Long idEquipo);
    /** Máximo 100 equipos distintos; líderes resueltos en batch por ID, no por posición. */
    List<SocialEquipoPerfilDto> obtenerPerfilesPorIds(List<Long> ids);
    List<SocialMiembroEquipoDto> equiposDeUsuario(Long idUsuario);
    boolean esMiembroDeEquipo(Long idUsuario, Long idEquipo);
    /** Cuenta y persona activas + Coordinador + membresía real en equipo activo. */
    boolean esAdminDeEquipo(Long idUsuario, Long idEquipo);
    List<SocialEquipoBusquedaDto> buscar(String query, int limite);
    /** IDs de usuario, no de persona; nunca incluye cuentas/personas/equipos inactivos. */
    List<Long> miembrosActivosDeEquipo(Long idEquipo);
}
