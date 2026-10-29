package upeu.edu.pe.Saludabemente.alertaclinica.integracion.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.alertaclinica.integracion.dto.PacienteContextoDto;
import upeu.edu.pe.Saludabemente.personal.api.PacienteContextoDTO;
import upeu.edu.pe.Saludabemente.personal.api.PersonalAlertasApiService;

/**
 * Envuelve la llamada al modulo Personal.
 * El motor nunca importa nada de personal.* excepto esta API publica.
 */
@Component
@RequiredArgsConstructor
public class PersonalClient {

    private final PersonalAlertasApiService personalApiService;

    public PacienteContextoDto obtenerContexto(Long idPersona) {
        PacienteContextoDTO dto = personalApiService.obtenerContexto(idPersona);
        return new PacienteContextoDto(
                dto.idPersona(),
                dto.nombreCompleto(),
                dto.areaTrabajo(),
                dto.sexo(),
                dto.fechaNacimiento(),
                dto.celularContacto(),
                dto.correoContacto()
        );
    }
}