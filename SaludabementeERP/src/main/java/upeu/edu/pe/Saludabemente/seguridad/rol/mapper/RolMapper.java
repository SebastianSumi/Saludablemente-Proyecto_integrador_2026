package upeu.edu.pe.Saludabemente.seguridad.rol.mapper;

import org.mapstruct.Mapper;
import upeu.edu.pe.Saludabemente.seguridad.rol.dto.response.RolResponse;
import upeu.edu.pe.Saludabemente.seguridad.rol.entity.Rol;

import java.util.Set;

/**
 * Mapeo entidad ↔ DTO del catálogo de roles.
 *
 * <p>Mapeo directo campo a campo. Sin lógica adicional: el catálogo de
 * roles es estático y no requiere transformaciones.</p>
 */
@Mapper(componentModel = "spring")
public interface RolMapper {

    RolResponse toResponse(Rol rol);

    Set<RolResponse> toResponseSet(Set<Rol> roles);
}