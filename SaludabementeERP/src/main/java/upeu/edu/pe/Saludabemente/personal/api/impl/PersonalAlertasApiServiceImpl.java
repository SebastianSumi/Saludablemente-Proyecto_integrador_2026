package upeu.edu.pe.Saludabemente.personal.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.personal.api.PacienteContextoDTO;
import upeu.edu.pe.Saludabemente.personal.api.PersonalAlertasApiService;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Persona;
import upeu.edu.pe.Saludabemente.personal.persona.repository.PersonaRepository;

@Service
@RequiredArgsConstructor
public class PersonalAlertasApiServiceImpl implements PersonalAlertasApiService {

    private final PersonaRepository personaRepository;

    @Override
    @Transactional(readOnly = true)
    public PacienteContextoDTO obtenerContexto(Long idPersona) {
        if (idPersona == null) {
            throw new IllegalArgumentException("El id de persona es obligatorio");
        }

        Persona persona = personaRepository.findById(idPersona)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Persona no encontrada: " + idPersona));

        return new PacienteContextoDTO(
                persona.getId(),
                construirNombreCompleto(persona),
                persona.getAreaTrabajo() != null
                        ? persona.getAreaTrabajo().getNombre()
                        : null,
                persona.getSexo() != null ? persona.getSexo().name() : null,
                persona.getFechaNacimiento(),
                persona.getCelular(),
                persona.getEmail()
        );
    }

    /**
     * Formato: "ApellidoPaterno ApellidoMaterno Nombres".
     * Si apellidoMaterno es null o vacio, se omite sin dejar espacio doble.
     */
    private String construirNombreCompleto(Persona p) {
        StringBuilder sb = new StringBuilder(p.getApellidoPaterno());
        if (p.getApellidoMaterno() != null && !p.getApellidoMaterno().isBlank()) {
            sb.append(' ').append(p.getApellidoMaterno());
        }
        sb.append(' ').append(p.getNombres());
        return sb.toString();
    }
}