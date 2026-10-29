package upeu.edu.pe.Saludabemente.personal.preferencia.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Persona;
import upeu.edu.pe.Saludabemente.personal.persona.service.PersonaService;
import upeu.edu.pe.Saludabemente.personal.preferencia.dto.NotificationRoutingDTO;
import upeu.edu.pe.Saludabemente.personal.preferencia.dto.request.PreferenciaRequest;
import upeu.edu.pe.Saludabemente.personal.preferencia.dto.response.PreferenciaResponse;
import upeu.edu.pe.Saludabemente.personal.preferencia.entity.PreferenciaComunicacion;
import upeu.edu.pe.Saludabemente.personal.preferencia.mapper.PreferenciaMapper;
import upeu.edu.pe.Saludabemente.personal.preferencia.repository.PreferenciaRepository;
import upeu.edu.pe.Saludabemente.personal.preferencia.service.PreferenciaService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PreferenciaServiceImpl implements PreferenciaService {

    private final PreferenciaRepository preferenciaRepository;
    private final PersonaService personaService;
    private final PreferenciaMapper preferenciaMapper;

    @Override
    @Transactional
    public PreferenciaResponse upsertPreferencias(Long idPersona, PreferenciaRequest request) {
        validarFranjaHoraria(request);

        // Resuelve la persona — lanza 404 si no existe
        Persona persona = personaService.buscarOFallar(idPersona);

        PreferenciaComunicacion preferencia = preferenciaRepository
                .findByPersonaId(idPersona)
                .orElseGet(() -> {
                    PreferenciaComunicacion nueva = preferenciaMapper.toEntity(request);
                    nueva.setPersona(persona);
                    return nueva;
                });

        // Solo actualiza los campos enviados — permite actualización parcial
        if (request.canalPreferido() != null) {
            preferencia.setCanalPreferido(request.canalPreferido());
        }
        if (request.horarioContactoInicio() != null) {
            preferencia.setHorarioContactoInicio(request.horarioContactoInicio());
        }
        if (request.horarioContactoFin() != null) {
            preferencia.setHorarioContactoFin(request.horarioContactoFin());
        }
        if (request.aceptaRecordatorios() != null) {
            preferencia.setAceptaRecordatorios(request.aceptaRecordatorios());
        }

        return preferenciaMapper.toResponse(preferenciaRepository.save(preferencia));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationRoutingDTO> getNotificationRoutingInfo(Long idPersona) {
        return preferenciaRepository.findByPersonaId(idPersona)
                .map(pref -> new NotificationRoutingDTO(
                        pref.getPersona().getId(),
                        pref.getPersona().getCelular(),
                        pref.getCanalPreferido(),
                        pref.getHorarioContactoInicio(),
                        pref.getHorarioContactoFin(),
                        pref.getAceptaRecordatorios()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public PreferenciaComunicacion buscarOFallar(Long idPersona) {
        return preferenciaRepository.findByPersonaId(idPersona)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Preferencia no encontrada para la persona: " + idPersona));
    }

    /**
     * Regla de negocio: si se envían ambos horarios, el inicio debe ser anterior al fin.
     * No es Bean Validation porque depende de una combinación de campos,
     * no de la forma de un campo individual.
     */
    private void validarFranjaHoraria(PreferenciaRequest request) {
        if (request.horarioContactoInicio() != null
                && request.horarioContactoFin() != null
                && !request.horarioContactoInicio().isBefore(request.horarioContactoFin())) {
            throw new BusinessRuleException(
                    "El horario de inicio debe ser anterior al horario de fin");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PreferenciaResponse obtenerPorPersona(Long idPersona) {
        return preferenciaMapper.toResponse(buscarOFallar(idPersona));
    }
}