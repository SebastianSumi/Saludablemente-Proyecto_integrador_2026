package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaSaltaSogaRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaSaltaSogaResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.entity.ReglaSaltaSoga;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.mapper.ReglaSaltaSogaMapper;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.repository.ReglaSaltaSogaRepository;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReglaSaltaSogaServiceImpl implements ReglaSaltaSogaService {

    private final ReglaSaltaSogaRepository repository;
    private final ReglaSaltaSogaMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ReglaSaltaSogaResponseDto> listar(EstadoRegla estado, SexoBiologico sexo) {
        List<ReglaSaltaSoga> reglas;
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
    public ReglaSaltaSogaResponseDto obtener(Long id) {
        return mapper.toResponseDto(buscarOFallar(id));
    }

    @Override
    @Transactional
    public ReglaSaltaSogaResponseDto crear(ReglaSaltaSogaRequestDto requestDto) {
        validarRangoInterno(requestDto);
        validarExclusividad(requestDto, null);

        ReglaSaltaSoga entidad = mapper.toEntity(requestDto);
        entidad.setEstado(EstadoRegla.ACTIVO);
        return mapper.toResponseDto(repository.save(entidad));
    }

    @Override
    @Transactional
    public ReglaSaltaSogaResponseDto actualizar(Long id, ReglaSaltaSogaRequestDto requestDto) {
        ReglaSaltaSoga entidad = buscarOFallar(id);
        validarRangoInterno(requestDto);
        validarExclusividad(requestDto, id);

        mapper.updateEntityFromDto(requestDto, entidad);
        return mapper.toResponseDto(repository.save(entidad));
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        ReglaSaltaSoga entidad = buscarOFallar(id);
        entidad.setEstado(EstadoRegla.INACTIVO);
        repository.save(entidad);
    }

    @Override
    @Transactional
    public ReglaSaltaSogaResponseDto reactivar(Long id) {
        ReglaSaltaSoga entidad = buscarOFallar(id);
        entidad.setEstado(EstadoRegla.ACTIVO);
        entidad.setFechaBaja(null);
        return mapper.toResponseDto(repository.save(entidad));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReglaSaltaSoga> buscarActivasParaMotor(SexoBiologico sexo, Integer edad) {
        return repository.findReglasActivasPorDemografia(sexo, edad);
    }

    private ReglaSaltaSoga buscarOFallar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ReglaSaltaSoga no encontrada: " + id));
    }

    private void validarRangoInterno(ReglaSaltaSogaRequestDto dto) {
        if (dto.edadMin() >= dto.edadMax()) {
            throw new BusinessRuleException(
                    "La edad minima debe ser menor a la edad maxima");
        }
        if (dto.saltosMin() >= dto.saltosMax()) {
            throw new BusinessRuleException(
                    "Los saltos minimos deben ser menores a los maximos");
        }
    }

    private void validarExclusividad(ReglaSaltaSogaRequestDto dto, Long idExcluir) {
        List<ReglaSaltaSoga> solapadas = repository.findSolapadas(
                dto.sexo(), dto.edadMin(), dto.edadMax(),
                dto.saltosMin(), dto.saltosMax(), idExcluir);
        if (!solapadas.isEmpty()) {
            throw new BusinessRuleException(
                    "Existe una regla activa que se solapa en sexo, rango de edad y rango de saltos");
        }
    }
}