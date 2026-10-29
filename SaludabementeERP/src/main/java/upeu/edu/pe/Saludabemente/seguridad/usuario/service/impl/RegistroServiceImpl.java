package upeu.edu.pe.Saludabemente.seguridad.usuario.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.personal.persona.dto.PersonaSnapshotIdentidadDTO;
import upeu.edu.pe.Saludabemente.seguridad.integracion.PersonalAdapter;
import upeu.edu.pe.Saludabemente.seguridad.rol.entity.Rol;
import upeu.edu.pe.Saludabemente.seguridad.rol.enums.RolEnum;
import upeu.edu.pe.Saludabemente.seguridad.rol.repository.RolRepository;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.request.RegistroRequest;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.request.UsuarioRequest;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.request.VerificarRegistroRequest;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.response.UsuarioResponse;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.response.VerificarRegistroResponse;
import upeu.edu.pe.Saludabemente.seguridad.usuario.repository.UsuarioRepository;
import upeu.edu.pe.Saludabemente.seguridad.usuario.service.RegistroService;
import upeu.edu.pe.Saludabemente.seguridad.usuario.service.UsuarioService;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistroServiceImpl implements RegistroService {

    private final PersonalAdapter personalAdapter;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioService usuarioService;

    @Override
    @Transactional(readOnly = true)
    public VerificarRegistroResponse verificar(VerificarRegistroRequest request) {
        PersonaSnapshotIdentidadDTO persona = buscarPersonaOblidar(request.documentoIdentidad());

        if (usuarioRepository.existsByIdPersonaSnapshot(persona.idPersona())) {
            throw new BusinessRuleException("YA_TIENE_CUENTA: la persona ya tiene una cuenta asignada");
        }
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("EMAIL_EN_USO: ya existe una cuenta con el email indicado");
        }

        String nombreMostrado = persona.nombres() + " " + persona.apellidoPaterno()
                + (persona.apellidoMaterno() == null || persona.apellidoMaterno().isBlank()
                        ? "" : " " + persona.apellidoMaterno());
        return new VerificarRegistroResponse(nombreMostrado.trim());
    }

    @Override
    public UsuarioResponse registrar(RegistroRequest request) {
        // Se re-resuelve en servidor: no se confía en ningún id enviado por el cliente.
        PersonaSnapshotIdentidadDTO persona = buscarPersonaOblidar(request.documentoIdentidad());

        Rol colaborador = rolRepository.findByNombre(RolEnum.COLABORADOR.getNombre())
                .orElseThrow(() -> new BusinessRuleException(
                        "Rol Colaborador no configurado en el sistema"));

        UsuarioRequest interno = new UsuarioRequest(
                request.email(),
                request.password(),
                persona.idPersona(),
                Set.of(colaborador.getId()));

        UsuarioResponse creado = usuarioService.crear(interno);
        log.info("Autorregistro público: email={}, idPersona={}",
                request.email(), persona.idPersona());
        return creado;
    }

    private PersonaSnapshotIdentidadDTO buscarPersonaOblidar(String documento) {
        try {
            return personalAdapter.buscarPorDocumento(documento);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Persona no encontrada en RRHH");
        }
    }
}
