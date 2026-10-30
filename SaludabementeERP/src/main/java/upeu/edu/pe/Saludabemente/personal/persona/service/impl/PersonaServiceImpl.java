package upeu.edu.pe.Saludabemente.personal.persona.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import upeu.edu.pe.Saludabemente.catalogo.area.entity.AreaTrabajo;
import upeu.edu.pe.Saludabemente.catalogo.area.service.AreaService;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.personal.credencial.service.CredencialService;
import upeu.edu.pe.Saludabemente.personal.persona.dto.PersonaAgeSexDTO;
import upeu.edu.pe.Saludabemente.personal.persona.dto.PersonaSnapshotClinicoDTO;
import upeu.edu.pe.Saludabemente.personal.persona.dto.PersonaSnapshotIdentidadDTO;
import upeu.edu.pe.Saludabemente.personal.persona.dto.PersonalAgregado;
import upeu.edu.pe.Saludabemente.personal.persona.dto.request.PersonaRequest;
import upeu.edu.pe.Saludabemente.personal.persona.dto.response.PersonaResponse;
import upeu.edu.pe.Saludabemente.personal.persona.dto.response.PersonaResumen;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Persona;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Sexo;
import upeu.edu.pe.Saludabemente.personal.persona.mapper.PersonaMapper;
import upeu.edu.pe.Saludabemente.personal.persona.repository.PersonaRepository;
import upeu.edu.pe.Saludabemente.personal.persona.service.PersonaService;
import upeu.edu.pe.Saludabemente.shared.storage.AlmacenamientoImagenService;
import upeu.edu.pe.Saludabemente.teams.entity.Team;
import upeu.edu.pe.Saludabemente.teams.service.TeamService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonaServiceImpl implements PersonaService {

    private final PersonaRepository personaRepository;
    private final PersonaMapper personaMapper;
    private final TeamService teamService;
    private final CredencialService credencialService;
    private final AreaService areaService;
    private final AlmacenamientoImagenService almacenamientoImagenService;

    private static final long MAX_AVATAR_BYTES = 2L * 1024 * 1024;

    // -------------------------------------------------------------------
    // Consultas
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<PersonaResponse> buscar(Boolean estado, Sexo sexo, Long areaTrabajoId,
                                        Long equipoId, String texto, Sort sort) {
        return personaRepository.buscar(estado, sexo, areaTrabajoId, equipoId, texto, sort)
                .stream()
                .map(personaMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PersonalAgregado reporte() {
        return personaRepository.agregados();
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaResponse obtener(Long id) {
        return personaMapper.toResponse(buscarOFallar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonaResumen> listarResumenPorEquipo(Long idEquipo) {
        teamService.buscarOFallar(idEquipo);
        return personaRepository.findByEquipoId(idEquipo)
                .stream()
                .map(personaMapper::toResumen)
                .toList();
    }

    // -------------------------------------------------------------------
    // Comandos
    // -------------------------------------------------------------------

    @Override
    @Transactional
    public PersonaResponse crear(PersonaRequest request) {
        validarCelularUnico(request.celular(), null);
        validarDocumentoUnico(request.documentoIdentidad(), null);

        Team equipo = resolverEquipo(request.equipoId());
        AreaTrabajo area = resolverArea(request.areaId());

        Persona persona = personaMapper.toEntity(request, equipo, area);
        persona.setActivo(true);

        return personaMapper.toResponse(personaRepository.save(persona));
    }

    @Override
    @Transactional
    public PersonaResponse actualizar(Long id, PersonaRequest request) {
        Persona persona = buscarOFallar(id);
        validarCelularUnico(request.celular(), persona.getId());
        validarDocumentoUnico(request.documentoIdentidad(), persona.getId());

        Team equipo = resolverEquipo(request.equipoId());
        AreaTrabajo area = resolverArea(request.areaId());

        personaMapper.updateEntity(request, equipo, area, persona);

        return personaMapper.toResponse(personaRepository.save(persona));
    }

    @Override
    @Transactional
    public void inactivar(Long id) {
        Persona persona = buscarOFallar(id);
        if (!Boolean.TRUE.equals(persona.getActivo())) {
            throw new BusinessRuleException("La persona ya está inactiva");
        }
        persona.setActivo(false);
        personaRepository.save(persona);
        credencialService.anonimizarCredenciales(id);
    }

    @Override
    @Transactional
    public PersonaResponse reactivar(Long id) {
        Persona persona = buscarOFallar(id);
        if (Boolean.TRUE.equals(persona.getActivo())) {
            throw new BusinessRuleException("La persona ya está activa");
        }
        persona.setActivo(true);
        PersonaResponse response = personaMapper.toResponse(personaRepository.save(persona));
        // La credencial se anonimiza al inactivar: se emite una nueva ACTIVA.
        credencialService.generarCredencial(id, false);
        return response;
    }

    @Override
    @Transactional
    public PersonaResponse cambiarCelular(Long id, String celular) {
        Persona persona = buscarOFallar(id);
        validarCelularUnico(celular, persona.getId());
        persona.setCelular(celular);
        return personaMapper.toResponse(personaRepository.save(persona));
    }

    @Override
    @Transactional
    public PersonaResponse subirAvatar(Long id, MultipartFile archivo) {
        Persona persona = buscarOFallar(id);
        eliminarObjetoAvatar(persona.getAvatarUrl());
        String objetoNuevo = almacenamientoImagenService.guardar(archivo, "avatares", MAX_AVATAR_BYTES);
        persona.setAvatarUrl(almacenamientoImagenService.construirUrlPublica(objetoNuevo));
        return personaMapper.toResponse(personaRepository.save(persona));
    }

    @Override
    @Transactional
    public PersonaResponse eliminarAvatar(Long id) {
        Persona persona = buscarOFallar(id);
        eliminarObjetoAvatar(persona.getAvatarUrl());
        persona.setAvatarUrl(null);
        return personaMapper.toResponse(personaRepository.save(persona));
    }

    private void eliminarObjetoAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) {
            return;
        }
        almacenamientoImagenService.eliminar(avatarUrl);
    }

    @Override
    @Transactional
    public List<PersonaResponse> bulkCreate(List<PersonaRequest> requests) {
        return requests.stream()
                .map(this::crearEnTransaccion)
                .toList();
    }

    // -------------------------------------------------------------------
    // Servicios publicos
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Persona buscarOFallar(Long id) {
        return personaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaSnapshotClinicoDTO obtenerSnapshotClinico(Long idPersona) {
        Persona persona = buscarOFallar(idPersona);
        Integer edad = personaMapper.calcularEdad(persona.getFechaNacimiento());

        return new PersonaSnapshotClinicoDTO(
                persona.getId(),
                persona.getDocumentoIdentidad(),
                construirNombreCompleto(persona),
                persona.getCelular(),
                edad,
                persona.getSexo().name(),
                persona.getAreaTrabajo() != null ? persona.getAreaTrabajo().getNombre() : null,
                Boolean.TRUE.equals(persona.getActivo())
        );
    }

    // -------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------

    private void validarCelularUnico(String celular, Long idActual) {
        if (idActual != null) {
            Persona actual = personaRepository.findById(idActual).orElse(null);
            if (actual != null && celular.equals(actual.getCelular())) {
                return;
            }
        }
        if (personaRepository.existsByCelularAndActivoTrue(celular)) {
            throw new BusinessRuleException(
                    "Ya existe una persona activa con el celular: " + celular);
        }
    }

    private void validarDocumentoUnico(String documento, Long idActual) {
        if (idActual != null) {
            Persona actual = personaRepository.findById(idActual).orElse(null);
            if (actual != null && documento.equals(actual.getDocumentoIdentidad())) {
                return;
            }
        }
        if (personaRepository.existsByDocumentoIdentidad(documento)) {
            throw new BusinessRuleException(
                    "Ya existe una persona con el documento de identidad: " + documento);
        }
    }

    private Team resolverEquipo(Long idEquipo) {
        return idEquipo == null ? null : teamService.buscarOFallar(idEquipo);
    }

    private AreaTrabajo resolverArea(Long idArea) {
        return idArea == null ? null : areaService.buscarOFallar(idArea);
    }

    private PersonaResponse crearEnTransaccion(PersonaRequest request) {
        validarCelularUnico(request.celular(), null);
        validarDocumentoUnico(request.documentoIdentidad(), null);
        Team equipo = resolverEquipo(request.equipoId());
        AreaTrabajo area = resolverArea(request.areaId());
        Persona persona = personaMapper.toEntity(request, equipo, area);
        persona.setActivo(true);
        return personaMapper.toResponse(personaRepository.save(persona));
    }

    private String construirNombreCompleto(Persona persona) {
        StringBuilder sb = new StringBuilder(persona.getNombres())
                .append(' ')
                .append(persona.getApellidoPaterno());
        if (persona.getApellidoMaterno() != null && !persona.getApellidoMaterno().isBlank()) {
            sb.append(' ').append(persona.getApellidoMaterno());
        }
        return sb.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> obtenerIdsPersonaPorCelulares(Set<String> celulares) {
        if (celulares == null || celulares.isEmpty()) {
            return Map.of();
        }
        return personaRepository.findIdsByCelulares(celulares).stream()
                .collect(Collectors.toMap(
                        PersonaRepository.CelularIdProjection::getCelular,
                        PersonaRepository.CelularIdProjection::getIdPersona
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaSnapshotIdentidadDTO obtenerSnapshotIdentidad(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id de persona es obligatorio");
        }

        Persona persona = buscarOFallar(id);

        return new PersonaSnapshotIdentidadDTO(
                persona.getId(),
                persona.getDocumentoIdentidad(),
                persona.getNombres(),
                persona.getApellidoPaterno(),
                persona.getApellidoMaterno(),
                persona.getAreaTrabajo() != null ? persona.getAreaTrabajo().getNombre() : null,
                persona.getEquipo() != null ? persona.getEquipo().getNombre() : null,  // ← NUEVO
                Boolean.TRUE.equals(persona.getActivo()),
                LocalDateTime.now()
        );
    }
}