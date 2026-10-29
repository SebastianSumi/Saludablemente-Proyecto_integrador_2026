package upeu.edu.pe.Saludabemente.seguridad.integracion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.personal.api.PersonalApiService;
import upeu.edu.pe.Saludabemente.personal.persona.dto.PersonaSnapshotIdentidadDTO;



import java.util.List;

/**
 * Capa anticorrupción entre el módulo Seguridad y el módulo Personal.
 *
 * <p><b>Responsabilidad:</b> aislar a Seguridad de los detalles del
 * contrato público de Personal. Ninguna clase fuera de este paquete
 * debe importar {@link PersonalApiService} ni {@link PersonaSnapshotIdentidadDTO}.</p>
 *
 * <p><b>Reglas arquitectónicas (verificadas por ModularityTests):</b></p>
 * <ul>
 *   <li>Seguridad NO importa {@code Persona}, {@code PersonaRepository},
 *       {@code PersonaResponse}, {@code Sexo}, {@code AreaResumen} ni
 *       {@code EquipoResumen}. Solo {@link PersonalApiService} y
 *       {@link PersonaSnapshotIdentidadDTO}.</li>
 *   <li>El snapshot es <b>inmutable</b>: se captura una vez al crear la
 *       cuenta y no se refresca salvo operación explícita (fuera de fase).</li>
 * </ul>
 *
 * <p><b>Traducción de errores:</b> el Adapter convierte
 * {@link ResourceNotFoundException} de Personal en
 * {@link BusinessRuleException} de Seguridad, para que el
 * {@code GlobalExceptionHandler} devuelva 409 (no 404). El "recurso"
 * solicitado por el cliente es el <em>usuario</em>, no la persona; que
 * la persona no exista es una violación de regla de negocio, no un 404
 * del recurso pedido.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PersonalAdapter {

    private final PersonalApiService personaService;

    /**
     * Obtiene el snapshot de identidad de una persona existente en RRHH.
     *
     * @param idPersona PK de la persona en el módulo Personal
     * @return snapshot inmutable con los 8 campos de identidad
     * @throws BusinessRuleException si la persona no existe en RRHH
     */
    public PersonaSnapshotIdentidadDTO obtenerSnapshot(Long idPersona) {
        try {
            return personaService.obtenerSnapshotIdentidad(idPersona);
        } catch (ResourceNotFoundException e) {
            log.warn("Persona no encontrada en RRHH. idPersona={}", idPersona);
            throw new BusinessRuleException("La persona no existe en RRHH");
        }
    }

    /**
     * Busca una persona activa por su documento de identidad.
     *
     * <p>Resuelve en memoria sobre los snapshots activos (sin tocar el
     * módulo Personal): las personas inactivas no aparecen y caen al
     * camino “persona no encontrada”.</p>
     *
     * @param documento documento de identidad (se compara sin espacios ni mayúsculas)
     * @return snapshot de la persona activa
     * @throws ResourceNotFoundException si no existe persona activa con ese documento
     */
    public PersonaSnapshotIdentidadDTO buscarPorDocumento(String documento) {
        String doc = documento == null ? "" : documento.trim();
        return listarPersonasDisponibles().stream()
                .filter(p -> p.documentoIdentidad() != null
                        && p.documentoIdentidad().trim().equalsIgnoreCase(doc))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Persona no encontrada en RRHH por documento");
                    return new ResourceNotFoundException("Persona no encontrada en RRHH");
                });
    }

    /**
     * Lista personas activas disponibles en Personal.
     * Devuelve lista vacía si Personal no tiene personas (no lanza excepción).
     */
    public List<PersonaSnapshotIdentidadDTO> listarPersonasDisponibles() {
        return personaService.listarSnapshotsIdentidadActivos();
    }
}