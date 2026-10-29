package upeu.edu.pe.Saludabemente.personal.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.personal.api.PersonalApiService;
import upeu.edu.pe.Saludabemente.personal.api.PersonalSnapshotDTO;
import upeu.edu.pe.Saludabemente.personal.persona.dto.PersonaSnapshotIdentidadDTO;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Persona;
import upeu.edu.pe.Saludabemente.personal.persona.repository.PersonaRepository;
import upeu.edu.pe.Saludabemente.personal.persona.service.PersonaService;
import upeu.edu.pe.Saludabemente.teams.entity.Team;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonalApiServiceImpl implements PersonalApiService {

    private final PersonaRepository personaRepository;
    private final PersonaService personaService;   // ← nuevo

    @Override
    @Transactional(readOnly = true)
    public PersonalSnapshotDTO validarExistencia(Long idPersona) {
        if (idPersona == null) return null;
        return personaRepository.findById(idPersona)
                .map(this::toSnapshot)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalSnapshotDTO> validarExistencias(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return personaRepository.findAllById(ids).stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .map(this::toSnapshot)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalSnapshotDTO> listarPersonalActivo() {
        Sort sort = Sort.by(Sort.Direction.ASC, "apellidoPaterno", "apellidoMaterno", "nombres");
        return personaRepository.findByActivoTrue(sort).stream()
                .map(this::toSnapshot)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PersonalSnapshotDTO obtenerSnapshot(Long idPersona) {
        if (idPersona == null) return null;
        return personaRepository.findById(idPersona)
                .map(this::toSnapshot)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalSnapshotDTO> obtenerSnapshots(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return personaRepository.findAllById(ids).stream()
                .map(this::toSnapshot)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalSnapshotDTO> listarPorTeam(Long idTeam) {
        if (idTeam == null) return List.of();
        return personaRepository.findByEquipoIdAndActivoTrue(idTeam).stream()
                .map(this::toSnapshot)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public int contarPersonasActivas(Long idTeam) {
        if (idTeam == null) return 0;
        return (int) personaRepository.countByEquipoIdAndActivoTrue(idTeam);
    }

    // -------------------------------------------------------------------
    // Mapper interno
    // -------------------------------------------------------------------

    private PersonalSnapshotDTO toSnapshot(Persona p) {
        Team equipo = p.getEquipo();
        return new PersonalSnapshotDTO(
                p.getId(),
                construirNombreCompleto(p),
                Boolean.TRUE.equals(p.getActivo()),
                equipo != null ? equipo.getId() : null,
                equipo != null ? equipo.getNombre() : null,
                p.getAreaTrabajo() != null ? p.getAreaTrabajo().getNombre() : null,
                p.getEmail(),
                p.getCelular(),
                p.getSexo() != null ? p.getSexo().name().charAt(0) : null,
                p.getFechaNacimiento()
        );
    }

    private String construirNombreCompleto(Persona p) {
        StringBuilder sb = new StringBuilder(p.getApellidoPaterno())
                .append(' ')
                .append(p.getNombres());
        if (p.getApellidoMaterno() != null && !p.getApellidoMaterno().isBlank()) {
            sb.insert(p.getApellidoPaterno().length(), ' ' + p.getApellidoMaterno());
        }
        return sb.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonaSnapshotIdentidadDTO> listarSnapshotsIdentidadActivos() {
        LocalDateTime snapshotFecha = LocalDateTime.now();
        Sort sort = Sort.by(
                Sort.Direction.ASC,
                "apellidoPaterno",
                "apellidoMaterno",
                "nombres"
        );

        return personaRepository.findByActivoTrue(sort).stream()
                .map(p -> toSnapshotIdentidad(p, snapshotFecha))
                .toList();
    }

    private PersonaSnapshotIdentidadDTO toSnapshotIdentidad(Persona p, LocalDateTime snapshotFecha) {
        return new PersonaSnapshotIdentidadDTO(
                p.getId(),
                p.getDocumentoIdentidad(),
                p.getNombres(),
                p.getApellidoPaterno(),
                p.getApellidoMaterno(),
                p.getAreaTrabajo() != null ? p.getAreaTrabajo().getNombre() : null,
                p.getEquipo() != null ? p.getEquipo().getNombre() : null,  // ← NUEVO
                Boolean.TRUE.equals(p.getActivo()),
                snapshotFecha
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaSnapshotIdentidadDTO obtenerSnapshotIdentidad(Long idPersona) {
        return personaService.obtenerSnapshotIdentidad(idPersona);
    }
}