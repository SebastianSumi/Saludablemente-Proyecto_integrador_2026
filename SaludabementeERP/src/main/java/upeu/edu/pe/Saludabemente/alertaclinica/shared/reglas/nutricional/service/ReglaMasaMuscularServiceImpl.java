package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaMasaMuscularRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaMasaMuscularResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaMasaMuscular;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.mapper.ReglaMasaMuscularMapper;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.repository.ReglaMasaMuscularRepository;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReglaMasaMuscularServiceImpl implements ReglaMasaMuscularService {

    private final ReglaMasaMuscularRepository repository;
    private final ReglaMasaMuscularMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ReglaMasaMuscularResponseDto> listar(EstadoRegla estado, SexoBiologico sexo) {
        List<ReglaMasaMuscular> reglas;
        if (estado != null && sexo != null) {
            reglas = repository.findByEstadoAndSexo(estado, sexo);
        } else if (estado != null) {
            reglas = repository.findByEstado(estado);
        } else if (sexo != null) {
            reglas = repository.findBySexo(sexo);
        } else {
            reglas = repository.findAll();
        }
        return mapper.toResponseDtoList(reglas);
    }

    @Override
    @Transactional(readOnly = true)
    public ReglaMasaMuscularResponseDto obtener(Long id) {
        return mapper.toResponseDto(buscarOFallar(id));
    }

    @Override
    @Transactional
    public ReglaMasaMuscularResponseDto crear(ReglaMasaMuscularRequestDto requestDto) {
        validarRangoInterno(requestDto);
        validarExclusividad(requestDto, null);

        ReglaMasaMuscular entidad = mapper.toEntity(requestDto);
        entidad.setEstado(EstadoRegla.ACTIVO);
        return mapper.toResponseDto(repository.save(entidad));
    }

    @Override
    @Transactional
    public ReglaMasaMuscularResponseDto actualizar(Long id, ReglaMasaMuscularRequestDto requestDto) {
        ReglaMasaMuscular entidad = buscarOFallar(id);
        validarRangoInterno(requestDto);
        validarExclusividad(requestDto, id);

        mapper.updateEntityFromDto(requestDto, entidad);
        return mapper.toResponseDto(repository.save(entidad));
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        ReglaMasaMuscular entidad = buscarOFallar(id);
        entidad.setEstado(EstadoRegla.INACTIVO);
        repository.save(entidad);
    }

    @Override
    @Transactional
    public ReglaMasaMuscularResponseDto reactivar(Long id) {
        ReglaMasaMuscular entidad = buscarOFallar(id);
        entidad.setEstado(EstadoRegla.ACTIVO);
        entidad.setFechaBaja(null);
        return mapper.toResponseDto(repository.save(entidad));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReglaMasaMuscular> buscarActivasParaMotor(SexoBiologico sexo, Integer edad) {
        return repository.findReglasActivasPorDemografia(sexo, edad);
    }

    private ReglaMasaMuscular buscarOFallar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ReglaMasaMuscular no encontrada: " + id));
    }

    private void validarRangoInterno(ReglaMasaMuscularRequestDto dto) {
        if (dto.edadMin() >= dto.edadMax()) {
            throw new BusinessRuleException(
                    "La edad minima debe ser menor a la edad maxima");
        }
        if (dto.porcentajeMin().compareTo(dto.porcentajeMax()) >= 0) {
            throw new BusinessRuleException(
                    "El porcentaje minimo debe ser menor al porcentaje maximo");
        }
    }

    private void validarExclusividad(ReglaMasaMuscularRequestDto dto, Long idExcluir) {
        List<ReglaMasaMuscular> solapadas = repository.findSolapadas(
                dto.sexo(), dto.edadMin(), dto.edadMax(),
                dto.porcentajeMin(), dto.porcentajeMax(), idExcluir);
        if (!solapadas.isEmpty()) {
            throw new BusinessRuleException(
                    "Existe una regla activa que se solapa en sexo, rango de edad y rango de masa muscular");
        }
    }
}