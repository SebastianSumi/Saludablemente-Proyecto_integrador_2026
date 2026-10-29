package upeu.edu.pe.Saludabemente.personal.persona.service;

import org.springframework.data.domain.Sort;
import upeu.edu.pe.Saludabemente.personal.persona.dto.PersonaAgeSexDTO;
import upeu.edu.pe.Saludabemente.personal.persona.dto.PersonaSnapshotClinicoDTO;
import upeu.edu.pe.Saludabemente.personal.persona.dto.PersonaSnapshotIdentidadDTO;
import upeu.edu.pe.Saludabemente.personal.persona.dto.PersonalAgregado;
import upeu.edu.pe.Saludabemente.personal.persona.dto.request.PersonaRequest;
import upeu.edu.pe.Saludabemente.personal.persona.dto.response.PersonaResponse;
import upeu.edu.pe.Saludabemente.personal.persona.dto.response.PersonaResumen;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Persona;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Sexo;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PersonaService {

    // --- Consultas internas ---
    List<PersonaResponse> buscar(Boolean estado, Sexo sexo, Long areaTrabajoId,
                                 Long equipoId, String texto, Sort sort);
    PersonalAgregado reporte();
    PersonaResponse obtener(Long id);
    List<PersonaResumen> listarResumenPorEquipo(Long idEquipo);

    // --- Comandos ---
    PersonaResponse crear(PersonaRequest request);
    PersonaResponse actualizar(Long id, PersonaRequest request);
    void inactivar(Long id);
    PersonaResponse reactivar(Long id);
    PersonaResponse cambiarCelular(Long id, String celular);
    PersonaResponse subirAvatar(Long id, org.springframework.web.multipart.MultipartFile archivo);
    PersonaResponse eliminarAvatar(Long id);
    List<PersonaResponse> bulkCreate(List<PersonaRequest> requests);

    // --- Servicios publicos ---
    Persona buscarOFallar(Long id);
    PersonaSnapshotClinicoDTO obtenerSnapshotClinico(Long id);
    /**
     * Resolucion batch de celulares a ids de persona. Solo devuelve
     * personas activas. Uso interno entre modulos del sistema
     * (importaciones masivas).
     *
     * @param celulares conjunto de celulares a resolver.
     * @return mapa celular -> idPersona. Vacio si {@code celulares} viene vacio o nulo.
     */
    Map<String, Long> obtenerIdsPersonaPorCelulares(Set<String> celulares);

    PersonaSnapshotIdentidadDTO obtenerSnapshotIdentidad(Long id);
}