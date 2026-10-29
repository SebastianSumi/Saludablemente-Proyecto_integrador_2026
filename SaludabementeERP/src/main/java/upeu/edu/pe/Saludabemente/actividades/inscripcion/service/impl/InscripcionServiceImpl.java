package upeu.edu.pe.Saludabemente.actividades.inscripcion.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.actividades.agenda.repository.ActividadRepository;
import upeu.edu.pe.Saludabemente.actividades.inscripcion.dto.response.InscritoDetalleResponse;
import upeu.edu.pe.Saludabemente.actividades.inscripcion.entity.ActividadInscrito;
import upeu.edu.pe.Saludabemente.actividades.inscripcion.entity.ActividadInscritoId;
import upeu.edu.pe.Saludabemente.actividades.inscripcion.repository.ActividadInscritoRepository;
import upeu.edu.pe.Saludabemente.actividades.inscripcion.service.InscripcionService;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.personal.api.PersonaActividadesDTO;
import upeu.edu.pe.Saludabemente.personal.api.PersonalActividadesApiService;


import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InscripcionServiceImpl implements InscripcionService {

    private final ActividadRepository actividadRepository;
    private final ActividadInscritoRepository inscritoRepository;
    private final PersonalActividadesApiService personalApiService;

    @Override
    @Transactional
    public int inscribirATodos(Long idActividad) {
        validarActividad(idActividad);

        // 1. Limpiar inscritos anteriores
        inscritoRepository.deleteByIdActividad(idActividad);

        // 2. Obtener todas las personas activas desde Personal
        List<Long> idsPersonas = personalApiService.listarPersonasActivasIds();

        // 3. Insertar en batch
        List<ActividadInscrito> nuevos = idsPersonas.stream()
                .map(idPersona -> new ActividadInscrito(idActividad, idPersona, true))
                .toList();

        inscritoRepository.saveAll(nuevos);
        return nuevos.size();
    }

    @Override
    @Transactional
    public int inscribirPersonas(Long idActividad, List<Long> idsPersonas) {
        validarActividad(idActividad);

        // Validar que las personas existan y esten activas
        if (idsPersonas != null && !idsPersonas.isEmpty()) {
            personalApiService.validarPersonas(idsPersonas);
        }

        if (idsPersonas == null || idsPersonas.isEmpty()) {
            return 0;
        }

        // Insertar solo los que aun no estan inscritos
        List<ActividadInscrito> nuevos = idsPersonas.stream()
                .distinct()
                .filter(id -> !inscritoRepository.existsById(
                        new ActividadInscritoId(idActividad, id)))
                .map(idPersona -> new ActividadInscrito(idActividad, idPersona, false))
                .toList();

        inscritoRepository.saveAll(nuevos);
        return nuevos.size();
    }

    @Override
    @Transactional
    public boolean quitarInscrito(Long idActividad, Long idPersona) {

        // 1. Validar que la actividad existe (lanza 404 si no)
        validarActividad(idActividad);

        // 2. Verificar que el inscrito existe
        ActividadInscritoId id = new ActividadInscritoId(idActividad, idPersona);

        if (!inscritoRepository.existsById(id)) {
            // No existia: no es error, pero devolvemos false
            return false;
        }

        // 3. Eliminar
        inscritoRepository.deleteById(id);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> listarInscritos(Long idActividad) {
        return inscritoRepository.findByIdActividad(idActividad)
                .stream()
                .map(ActividadInscrito::getIdPersona)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InscritoDetalleResponse> listarInscritosDetalle(Long idActividad) {
        List<ActividadInscrito> inscritos = inscritoRepository.findByIdActividad(idActividad);
        if (inscritos.isEmpty()) {
            return List.of();
        }

        List<Long> idsPersonas = inscritos.stream()
                .map(ActividadInscrito::getIdPersona)
                .toList();

        Map<Long, PersonaActividadesDTO> personas = personalApiService
                .listarPersonasPorIds(idsPersonas)
                .stream()
                .collect(Collectors.toMap(PersonaActividadesDTO::idPersona, Function.identity()));

        return inscritos.stream()
                .map(i -> toDetalle(i, personas.get(i.getIdPersona())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaInscrito(Long idActividad, Long idPersona) {
        if (idActividad == null || idPersona == null) {
            return false;
        }
        return inscritoRepository.existsById(new ActividadInscritoId(idActividad, idPersona));
    }

    @Override
    @Transactional(readOnly = true)
    public long contarInscritos(Long idActividad) {
        return inscritoRepository.countByIdActividad(idActividad);
    }

    private InscritoDetalleResponse toDetalle(ActividadInscrito i, PersonaActividadesDTO p) {
        if (p == null) {
            return new InscritoDetalleResponse(
                    i.getIdPersona(),
                    "Persona #" + i.getIdPersona(),
                    null, null, null, null, null, null, null,
                    i.getInscritoPorTodos(),
                    i.getFechaInscripcion());
        }
        return new InscritoDetalleResponse(
                i.getIdPersona(),
                p.nombreCompleto(),
                p.nombres(),
                p.apellidoPaterno(),
                p.apellidoMaterno(),
                p.areaTrabajo(),
                p.celular(),
                p.sexo(),
                p.activo(),
                i.getInscritoPorTodos(),
                i.getFechaInscripcion());
    }

    private void validarActividad(Long idActividad) {
        if (!actividadRepository.existsById(idActividad)) {
            throw new ResourceNotFoundException("Actividad no encontrada: " + idActividad);
        }
    }
}