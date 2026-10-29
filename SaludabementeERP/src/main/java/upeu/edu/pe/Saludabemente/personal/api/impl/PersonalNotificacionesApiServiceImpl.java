package upeu.edu.pe.Saludabemente.personal.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.personal.api.DatosBasicosPersonaDTO;
import upeu.edu.pe.Saludabemente.personal.api.PersonalNotificacionesApiService;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Persona;
import upeu.edu.pe.Saludabemente.personal.persona.repository.PersonaRepository;

@Service
@RequiredArgsConstructor
public class PersonalNotificacionesApiServiceImpl implements PersonalNotificacionesApiService {

    private final PersonaRepository personaRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean existeYEstaActiva(Long idPersona) {
        if (idPersona == null) return false;
        return personaRepository.existsByIdAndActivoTrue(idPersona);
    }

    @Override
    @Transactional(readOnly = true)
    public DatosBasicosPersonaDTO obtenerDatosBasicos(Long idPersona) {
        if (idPersona == null) {
            throw new IllegalArgumentException("El id de persona es obligatorio");
        }

        Persona persona = personaRepository.findById(idPersona)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Persona no encontrada: " + idPersona));

        if (!Boolean.TRUE.equals(persona.getActivo())) {
            throw new ResourceNotFoundException(
                    "Persona inactiva: " + idPersona);
        }

        return new DatosBasicosPersonaDTO(
                persona.getId(),
                persona.getNombres(),
                construirApellidos(persona),
                persona.getSexo() != null ? persona.getSexo().name() : null,
                persona.getFechaNacimiento(),
                persona.getEquipo() != null ? persona.getEquipo().getId() : null
        );
    }

    private String construirApellidos(Persona p) {
        StringBuilder sb = new StringBuilder(p.getApellidoPaterno());
        if (p.getApellidoMaterno() != null && !p.getApellidoMaterno().isBlank()) {
            sb.append(' ').append(p.getApellidoMaterno());
        }
        return sb.toString();
    }
}