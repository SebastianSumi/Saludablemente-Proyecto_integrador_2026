package upeu.edu.pe.Saludabemente.seguridad.rol.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.seguridad.rol.dto.response.RolResponse;
import upeu.edu.pe.Saludabemente.seguridad.rol.entity.Rol;
import upeu.edu.pe.Saludabemente.seguridad.rol.enums.RolEnum;
import upeu.edu.pe.Saludabemente.seguridad.rol.mapper.RolMapper;
import upeu.edu.pe.Saludabemente.seguridad.rol.repository.RolRepository;
import upeu.edu.pe.Saludabemente.seguridad.rol.service.RolService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementación de {@link RolService}.
 *
 * <p>Aplica filtro para exponer y permitir únicamente los roles definidos en {@link RolEnum}.</p>
 */
@Service
@RequiredArgsConstructor
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;
    private final RolMapper rolMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RolResponse> listarTodos() {
        return rolRepository.findAll().stream()
                .filter(r -> RolEnum.esValido(r.getNombre()))
                .map(rolMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Rol> validarRolesExisten(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessRuleException("Debe indicar al menos un rol");
        }

        List<Rol> encontrados = rolRepository.findByIdIn(ids);

        if (encontrados.size() != ids.size()) {
            Set<Long> encontradosIds = new HashSet<>();
            encontrados.forEach(r -> encontradosIds.add(r.getId()));

            Set<Long> faltantes = new HashSet<>(ids);
            faltantes.removeAll(encontradosIds);

            throw new BusinessRuleException(
                    "Roles inexistentes: " + faltantes);
        }

        // Validar que ninguno de los roles sea un rol no soportado/fantasma fuera del enum
        for (Rol rol : encontrados) {
            if (!RolEnum.esValido(rol.getNombre())) {
                throw new BusinessRuleException("El rol '" + rol.getNombre() + "' no es un rol válido del sistema");
            }
        }

        return new HashSet<>(encontrados);
    }
}