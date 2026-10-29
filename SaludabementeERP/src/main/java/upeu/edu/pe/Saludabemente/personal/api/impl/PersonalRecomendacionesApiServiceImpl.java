package upeu.edu.pe.Saludabemente.personal.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.personal.api.PersonaRecomendacionesDTO;
import upeu.edu.pe.Saludabemente.personal.api.PersonalRecomendacionesApiService;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Persona;
import upeu.edu.pe.Saludabemente.personal.persona.repository.PersonaRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PersonalRecomendacionesApiServiceImpl implements PersonalRecomendacionesApiService {

    private final PersonaRepository personaRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<PersonaRecomendacionesDTO> obtenerDatosDemograficos(Long idPersona) {
        if (idPersona == null) return Optional.empty();

        return personaRepository.findById(idPersona).map(this::toDTO);
    }

    private PersonaRecomendacionesDTO toDTO(Persona p) {
        return new PersonaRecomendacionesDTO(
                p.getId(),
                p.getNombres(),
                p.getApellidoPaterno(),
                p.getApellidoMaterno(),
                p.getSexo() != null ? p.getSexo().name() : null,
                p.getFechaNacimiento(),
                Boolean.TRUE.equals(p.getActivo())
        );
    }
}