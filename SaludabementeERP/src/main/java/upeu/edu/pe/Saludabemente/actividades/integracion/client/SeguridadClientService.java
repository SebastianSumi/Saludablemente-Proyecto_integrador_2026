package upeu.edu.pe.Saludabemente.actividades.integracion.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.actividades.integracion.dto.CoordinadorSnapshotDto;
import upeu.edu.pe.Saludabemente.actividades.integracion.dto.UsuarioActividadDto;
import upeu.edu.pe.Saludabemente.seguridad.api.SeguridadService;
import upeu.edu.pe.Saludabemente.seguridad.api.UsuarioSnapshotDto;

import java.util.List;

/**
 * Capa anticorrupcion entre Actividades y Seguridad.
 * Es el UNICO lugar del modulo Actividades que puede importar
 * clases de seguridad.api.*
 */
@Component
@RequiredArgsConstructor
public class SeguridadClientService {

    private final SeguridadService seguridadService;

    /**
     * Valida que el usuario exista, este activo y tenga
     * rol Coordinador o Evaluador.
     * Lanza excepcion si falla (404 o 409).
     */
    public void validarCoordinador(Long idUsuario) {
        seguridadService.validarCoordinador(idUsuario);
    }

    /**
     * Obtiene los datos minimos del coordinador para mostrar en la respuesta.
     */
    public CoordinadorSnapshotDto obtenerCoordinador(Long idUsuario) {
        UsuarioSnapshotDto usuario = seguridadService.obtenerUsuarioPorId(idUsuario);
        return new CoordinadorSnapshotDto(
                usuario.idUsuario(),
                usuario.nombreCompleto(),
                usuario.email()
        );
    }

    /**
     * Obtiene los datos de un usuario incluyendo su rol principal.
     */
    public UsuarioActividadDto obtenerUsuario(Long idUsuario) {
        UsuarioSnapshotDto usuario = seguridadService.obtenerUsuarioPorId(idUsuario);
        String rol = (usuario.roles() != null && !usuario.roles().isEmpty())
                ? usuario.roles().get(0)
                : null;
        return new UsuarioActividadDto(
                usuario.idUsuario(),
                usuario.nombreCompleto(),
                usuario.email(),
                rol
        );
    }

    /**
     * Lista todos los usuarios activos con rol Coordinador.
     * Util para poblar el combo del frontend en S2.
     */
    public List<CoordinadorSnapshotDto> listarCoordinadores() {
        return listarUsuariosPorRol("Coordinador");
    }

    /**
     * Lista todos los usuarios activos con el rol indicado, ordenados por nombre.
     * El orden es determinista (lo garantiza SeguridadService).
     * Util para que los seeders resuelvan ids reales sin hardcodear.
     */
    public List<CoordinadorSnapshotDto> listarUsuariosPorRol(String rol) {
        return seguridadService.listarUsuariosPorRol(rol)
                .stream()
                .map(u -> new CoordinadorSnapshotDto(
                        u.idUsuario(),
                        u.nombreCompleto(),
                        u.email()
                ))
                .toList();
    }

    public CoordinadorSnapshotDto obtenerCoordinadorOpcional(Long idUsuario) {
        try {
            return obtenerCoordinador(idUsuario);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Verifica si un usuario esta activo.
     * Devuelve false si no existe o esta inactivo (no lanza excepcion).
     */
    public boolean validarUsuarioActivo(Long idUsuario) {
        return seguridadService.validarUsuarioActivo(idUsuario);
    }
}