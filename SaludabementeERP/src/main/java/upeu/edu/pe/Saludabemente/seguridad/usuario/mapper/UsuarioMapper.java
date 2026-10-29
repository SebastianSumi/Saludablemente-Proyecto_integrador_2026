package upeu.edu.pe.Saludabemente.seguridad.usuario.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import upeu.edu.pe.Saludabemente.personal.persona.dto.PersonaSnapshotIdentidadDTO;
import upeu.edu.pe.Saludabemente.seguridad.rol.mapper.RolMapper;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.request.UsuarioRequest;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.response.UsuarioResponse;
import upeu.edu.pe.Saludabemente.seguridad.usuario.entity.Usuario;

/**
 * Mapeo entidad ↔ DTO del módulo Seguridad.
 *
 * <p><b>Regla arquitectónica:</b> este mapper es el único punto autorizado
 * para importar {@code PersonaSnapshotIdentidadDTO} desde el módulo
 * Personal. El resto del módulo Seguridad no debe importarlo — solo el
 * {@code PersonalAdapter} y este mapper.</p>
 *
 * <p><b>Nota sobre password:</b> {@code toEntity} ignora los campos
 * {@code passwordHash}, {@code roles} y {@code fechaCreacion} — los
 * completa el servicio con {@code CifradoUtil}, la validación de roles
 * y {@code LocalDateTime.now()} respectivamente.</p>
 */
@Mapper(
        componentModel = "spring",
        uses = {RolMapper.class}
)
public interface UsuarioMapper {

    /**
     * Construye la entidad {@link Usuario} a partir del request y del
     * snapshot de identidad capturado desde Personal.
     *
     * <p>Campos ignorados a propósito (los completa el servicio):</p>
     * <ul>
     *   <li>{@code id} — autogenerado por la BD</li>
     *   <li>{@code passwordHash} — se cifra con BCrypt en el servicio</li>
     *   <li>{@code roles} — se resuelven desde {@code RolRepository}</li>
     *   <li>{@code activo} — se inicializa en {@code true} vía @Builder.Default</li>
     *   <li>{@code fechaCreacion} — se inicializa con {@code LocalDateTime.now()}</li>
     * </ul>
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "username", ignore = true)
    // --- Snapshot de identidad desde PersonaSnapshotIdentidadDTO ---
    @Mapping(source = "identidad.idPersona", target = "idPersonaSnapshot")
    @Mapping(source = "identidad.documentoIdentidad", target = "documentoIdentidadSnapshot")
    @Mapping(source = "identidad.nombres", target = "nombresSnapshot")
    @Mapping(source = "identidad.apellidoPaterno", target = "apellidoPaternoSnapshot")
    @Mapping(source = "identidad.apellidoMaterno", target = "apellidoMaternoSnapshot")
    @Mapping(source = "identidad.areaTrabajo", target = "areaTrabajoSnapshot")
    @Mapping(source = "identidad.activoPersonaSnapshot", target = "activoPersonaSnapshot")
    @Mapping(source = "identidad.snapshotFecha", target = "snapshotFecha")
    @Mapping(source = "identidad.nombreEquipo", target = "equipoSnapshot")
    Usuario toEntity(UsuarioRequest request, PersonaSnapshotIdentidadDTO identidad);

    /**
     * Construye el response 360° a partir de la entidad ya persistida.
     *
     * <p>El mapeo de {@code roles} usa {@link RolMapper} vía {@code uses}.
     * Los campos snapshot de la entidad se exponen con el mismo nombre
     * del response (nombres, apellidoPaterno, etc.) — sin el sufijo
     * {@code *Snapshot}, porque el DTO es la representación de negocio,
     * no la entidad.</p>
     */
    @Mapping(source = "idPersonaSnapshot", target = "idPersona")
    @Mapping(source = "documentoIdentidadSnapshot", target = "documentoIdentidad")
    @Mapping(source = "nombresSnapshot", target = "nombres")
    @Mapping(source = "apellidoPaternoSnapshot", target = "apellidoPaterno")
    @Mapping(source = "apellidoMaternoSnapshot", target = "apellidoMaterno")
    @Mapping(source = "areaTrabajoSnapshot", target = "areaTrabajo")
    @Mapping(source = "equipoSnapshot", target = "equipo")
    @Mapping(source = "activoPersonaSnapshot", target = "activoPersona")
    @Mapping(source = "snapshotFecha", target = "snapshotFecha")
    @Mapping(source = "roles", target = "roles")
    UsuarioResponse toResponse(Usuario usuario);
}