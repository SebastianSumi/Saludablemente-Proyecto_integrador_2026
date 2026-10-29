package upeu.edu.pe.Saludabemente.personal.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.personal.api.PersonaActividadesDTO;
import upeu.edu.pe.Saludabemente.personal.api.PersonalActividadesApiService;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Persona;
import upeu.edu.pe.Saludabemente.personal.persona.repository.PersonaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonalActividadesApiServiceImpl implements PersonalActividadesApiService {

    private final PersonaRepository personaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Long> listarPersonasActivasIds() {
        return personaRepository.findIdsByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonaActividadesDTO> listarPersonasActivas() {
        Sort sort = Sort.by(
                Sort.Direction.ASC,
                "apellidoPaterno",
                "apellidoMaterno",
                "nombres"
        );
        return personaRepository.findByActivoTrue(sort).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonaActividadesDTO> listarPersonasPorIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return personaRepository.findAllById(ids).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public void validarPersonas(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        List<Persona> encontradas = personaRepository.findAllById(ids);

        if (encontradas.size() != ids.size()) {
            throw new BusinessRuleException("Una o mas personas no existen");
        }

        List<Long> inactivas = encontradas.stream()
                .filter(p -> !Boolean.TRUE.equals(p.getActivo()))
                .map(Persona::getId)
                .toList();

        if (!inactivas.isEmpty()) {
            throw new BusinessRuleException(
                    "Las siguientes personas estan inactivas: " + inactivas);
        }
    }

    // -------------------------------------------------------------------
    // Mapper interno
    // -------------------------------------------------------------------

    private PersonaActividadesDTO toDTO(Persona p) {
        return new PersonaActividadesDTO(
                p.getId(),
                p.getNombres(),
                p.getApellidoPaterno(),
                p.getApellidoMaterno(),
                construirNombreCompleto(p),
                p.getAreaTrabajo() != null ? p.getAreaTrabajo().getNombre() : null,
                p.getCelular(),
                p.getSexo() != null ? p.getSexo().name() : null,
                Boolean.TRUE.equals(p.getActivo())
        );
    }

    /**
     * Formato: "Nombres ApellidoPaterno ApellidoMaterno".
     * Si apellidoMaterno es null o vacio, se omite sin dejar espacio doble.
     */
    private String construirNombreCompleto(Persona p) {
        StringBuilder sb = new StringBuilder(p.getNombres())
                .append(' ')
                .append(p.getApellidoPaterno());
        if (p.getApellidoMaterno() != null && !p.getApellidoMaterno().isBlank()) {
            sb.append(' ').append(p.getApellidoMaterno());
        }
        return sb.toString();
    }
}