package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaCarrera400mRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaCarrera400mResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.entity.ReglaCarrera400m;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.mapper.ReglaCarrera400mMapper;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.repository.ReglaCarrera400mRepository;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReglaCarrera400mServiceImpl implements ReglaCarrera400mService {

    private final ReglaCarrera400mRepository repository;
    private final ReglaCarrera400mMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ReglaCarrera400mResponseDto> listar(EstadoRegla estado, SexoBiologico sexo) {
        List<ReglaCarrera400m> reglas;
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
    public ReglaCarrera400mResponseDto obtener(Long id) {
        return mapper.toResponseDto(buscarOFallar(id));
    }

    @Override
    @Transactional
    public ReglaCarrera400mResponseDto crear(ReglaCarrera400mRequestDto requestDto) {
        validarRangoInterno(requestDto);
        validarExclusividad(requestDto, null);

        ReglaCarrera400m entidad = mapper.toEntity(requestDto);
        entidad.setEstado(EstadoRegla.ACTIVO);
        return mapper.toResponseDto(repository.save(entidad));
    }

    @Override
    @Transactional
    public ReglaCarrera400mResponseDto actualizar(Long id, ReglaCarrera400mRequestDto requestDto) {
        ReglaCarrera400m entidad = buscarOFallar(id);
        validarRangoInterno(requestDto);
        validarExclusividad(requestDto, id);

        mapper.updateEntityFromDto(requestDto, entidad);
        return mapper.toResponseDto(repository.save(entidad));
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        ReglaCarrera400m entidad = buscarOFallar(id);
        entidad.setEstado(EstadoRegla.INACTIVO);
        repository.save(entidad);
    }

    @Override
    @Transactional
    public ReglaCarrera400mResponseDto reactivar(Long id) {
        ReglaCarrera400m entidad = buscarOFallar(id);
        entidad.setEstado(EstadoRegla.ACTIVO);
        entidad.setFechaBaja(null);
        return mapper.toResponseDto(repository.save(entidad));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReglaCarrera400m> buscarActivasParaMotor(SexoBiologico sexo, Integer edad) {
        return repository.findReglasActivasPorDemografia(sexo, edad);
    }

    private ReglaCarrera400m buscarOFallar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ReglaCarrera400m no encontrada: " + id));
    }

    private void validarRangoInterno(ReglaCarrera400mRequestDto dto) {
        if (dto.edadMin() >= dto.edadMax()) {
            throw new BusinessRuleException(
                    "La edad minima debe ser menor a la edad maxima");
        }
        if (dto.tiempoSegundosMin() >= dto.tiempoSegundosMax()) {
            throw new BusinessRuleException(
                    "El tiempo minimo debe ser menor al tiempo maximo");
        }
    }

    private void validarExclusividad(ReglaCarrera400mRequestDto dto, Long idExcluir) {
        List<ReglaCarrera400m> solapadas = repository.findSolapadas(
                dto.sexo(), dto.edadMin(), dto.edadMax(),
                dto.tiempoSegundosMin(), dto.tiempoSegundosMax(), idExcluir);
        if (!solapadas.isEmpty()) {
            throw new BusinessRuleException(
                    "Existe una regla activa que se solapa en sexo, rango de edad y rango de tiempo");
        }
    }
}