package upeu.edu.pe.Saludabemente.seguridad.usuario.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.personal.persona.dto.PersonaSnapshotIdentidadDTO;
import upeu.edu.pe.Saludabemente.seguridad.integracion.PersonalAdapter;
import upeu.edu.pe.Saludabemente.seguridad.rol.entity.Rol;
import upeu.edu.pe.Saludabemente.seguridad.rol.service.RolService;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.request.PasswordRequest;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.request.RolesRequest;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.request.UsuarioRequest;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.response.UsuarioResponse;
import upeu.edu.pe.Saludabemente.seguridad.usuario.entity.Usuario;
import upeu.edu.pe.Saludabemente.seguridad.usuario.mapper.UsuarioMapper;
import upeu.edu.pe.Saludabemente.seguridad.usuario.repository.UsuarioRepository;
import upeu.edu.pe.Saludabemente.seguridad.usuario.service.UsuarioService;
import upeu.edu.pe.Saludabemente.seguridad.util.CifradoUtil;
import upeu.edu.pe.Saludabemente.seguridad.util.UsernameGenerator;

import java.util.Set;

/**
 * Implementación de {@link UsuarioService}.
 *
 * <p><b>Sub-fase 2 pendiente:</b> guard clauses de ROOT y suplantación.
 * El repositorio ya expone {@code esRoot(id)} y el filtro fantasma ya
 * está en {@code buscarDinamico} — solo falta cablearlos.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolService rolService;
    private final PersonalAdapter personalAdapter;
    private final UsuarioMapper usuarioMapper;
    private final CifradoUtil cifradoUtil;

    // -------------------------------------------------------------------
    // Crear
    // -------------------------------------------------------------------

    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {

        // 1-5. Validaciones (sin cambios)
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException(
                    "Ya existe una cuenta con el email: " + request.email());
        }
        if (usuarioRepository.existsByIdPersonaSnapshot(request.idPersona())) {
            throw new BusinessRuleException("La persona ya tiene una cuenta asignada");
        }

        PersonaSnapshotIdentidadDTO identidad =
                personalAdapter.obtenerSnapshot(request.idPersona());

        if (!identidad.activoPersonaSnapshot()) {
            throw new BusinessRuleException(
                    "No se puede crear cuenta para una persona inactiva en RRHH");
        }

        Set<Rol> roles = rolService.validarRolesExisten(request.rolesIds());

        // 6-7. Construir entidad (sin cambios)
        Usuario usuario = usuarioMapper.toEntity(request, identidad);
        usuario.setPasswordHash(cifradoUtil.hash(request.password()));
        usuario.setRoles(roles);
        usuario.setActivo(true);

        // 7b. Generar username único
        String username = UsernameGenerator.generarUnico(
                identidad.nombres(),
                identidad.apellidoPaterno(),
                usuarioRepository::existsByUsername
        );
        usuario.setUsername(username);

        // 8. Persistir en Oracle. La contraseña ya viaja cifrada en la entidad.
        Usuario guardado = usuarioRepository.save(usuario);

        log.info("Usuario creado: id={}, email={}, idPersona={}",
                guardado.getId(), guardado.getEmail(), guardado.getIdPersonaSnapshot());

        return usuarioMapper.toResponse(guardado);
    }

    // -------------------------------------------------------------------
    // Buscar / obtener
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResponse> buscar(String email, Boolean activo, Pageable pageable) {
        // Sub-fase 1: excluirRoot siempre false (ROOT todavía no existe).
        // Sub-fase 2: se cambia por !currentUserProvider.isRoot().
        boolean excluirRoot = false;

        return usuarioRepository
                .buscarDinamico(email, activo, excluirRoot, pageable)
                .map(usuarioMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        Usuario usuario = buscarOFallar(id);
        return usuarioMapper.toResponse(usuario);
    }

    // -------------------------------------------------------------------
    // Actualizar roles
    // -------------------------------------------------------------------

    @Override
    @Transactional
    public UsuarioResponse actualizarRoles(Long id, RolesRequest request) {
        Usuario usuario = buscarOFallar(id);

        // Sub-fase 2 agregará aquí la guard clause de ROOT:
        //   if (usuario.tieneRol("ROOT") && !currentUserProvider.isRoot()) {
        //       throw new BusinessRuleException(...);
        //   }

        Set<Rol> nuevosRoles = rolService.validarRolesExisten(request.rolesIds());

        usuario.getRoles().clear();
        usuario.getRoles().addAll(nuevosRoles);
        usuarioRepository.save(usuario);

        log.info("Roles actualizados: idUsuario={}, roles={}",
                id, nuevosRoles.stream().map(Rol::getNombre).toList());

        return usuarioMapper.toResponse(usuario);
    }

    // -------------------------------------------------------------------
    // Actualizar contraseña
    // -------------------------------------------------------------------

    @Override
    @Transactional
    public void actualizarPassword(Long id, PasswordRequest request) {
        Usuario usuario = buscarOFallar(id);

        // Sub-fase 2 agregará aquí la guard clause de ROOT.

        usuario.setPasswordHash(cifradoUtil.hash(request.newPassword()));
        usuarioRepository.save(usuario);

        log.info("Contraseña actualizada: idUsuario={}", id);
    }

    // -------------------------------------------------------------------
    // Inactivar / reactivar
    // -------------------------------------------------------------------

    @Override
    @Transactional
    public void inactivar(Long id) {
        Usuario usuario = buscarOFallar(id);

        // Sub-fase 2 agregará aquí la guard clause de ROOT.

        if (!usuario.getActivo()) {
            throw new BusinessRuleException("La cuenta ya está inactiva");
        }

        usuario.setActivo(false);
        usuarioRepository.save(usuario);

        log.info("Cuenta inactivada: idUsuario={}", id);
    }

    @Override
    @Transactional
    public void reactivar(Long id) {
        Usuario usuario = buscarOFallar(id);

        if (usuario.getActivo()) {
            throw new BusinessRuleException("La cuenta ya está activa");
        }

        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        log.info("Cuenta reactivada: idUsuario={}", id);
    }

    // -------------------------------------------------------------------
    // Utilitario
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarOFallar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con id: " + id));
    }

}