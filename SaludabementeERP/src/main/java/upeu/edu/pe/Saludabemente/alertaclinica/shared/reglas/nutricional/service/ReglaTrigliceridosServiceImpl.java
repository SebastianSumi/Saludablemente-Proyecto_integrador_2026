package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaTrigliceridosRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaTrigliceridosResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaTrigliceridos;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.mapper.ReglaTrigliceridosMapper;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.repository.ReglaTrigliceridosRepository;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReglaTrigliceridosServiceImpl implements ReglaTrigliceridosService {

    private final ReglaTrigliceridosRepository repository;
    private final ReglaTrigliceridosMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ReglaTrigliceridosResponseDto> listar(EstadoRegla estado, SexoBiologico sexo) {
        List<ReglaTrigliceridos> reglas;
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
    public ReglaTrigliceridosResponseDto obtener(Long id) {
        return mapper.toResponseDto(buscarOFallar(id));
    }

    @Override
    @Transactional
    public ReglaTrigliceridosResponseDto crear(ReglaTrigliceridosRequestDto requestDto) {
        validarRangoInterno(requestDto);
        validarExclusividad(requestDto, null);

        ReglaTrigliceridos entidad = mapper.toEntity(requestDto);
        entidad.setEstado(EstadoRegla.ACTIVO);
        return mapper.toResponseDto(repository.save(entidad));
    }

    @Override
    @Transactional
    public ReglaTrigliceridosResponseDto actualizar(Long id, ReglaTrigliceridosRequestDto requestDto) {
        ReglaTrigliceridos entidad = buscarOFallar(id);
        validarRangoInterno(requestDto);
        validarExclusividad(requestDto, id);

        mapper.updateEntityFromDto(requestDto, entidad);
        return mapper.toResponseDto(repository.save(entidad));
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        ReglaTrigliceridos entidad = buscarOFallar(id);
        entidad.setEstado(EstadoRegla.INACTIVO);
        repository.save(entidad);
    }

    @Override
    @Transactional
    public ReglaTrigliceridosResponseDto reactivar(Long id) {
        ReglaTrigliceridos entidad = buscarOFallar(id);
        entidad.setEstado(EstadoRegla.ACTIVO);
        entidad.setFechaBaja(null);
        return mapper.toResponseDto(repository.save(entidad));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReglaTrigliceridos> buscarActivasParaMotor(SexoBiologico sexo, Integer edad) {
        return repository.findReglasActivasPorDemografia(sexo, edad);
    }

    private ReglaTrigliceridos buscarOFallar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ReglaTrigliceridos no encontrada: " + id));
    }

    private void validarRangoInterno(ReglaTrigliceridosRequestDto dto) {
        if (dto.edadMin() >= dto.edadMax()) {
            throw new BusinessRuleException(
                    "La edad minima debe ser menor a la edad maxima");
        }
        if (dto.trigliceridosMgDlMin().compareTo(dto.trigliceridosMgDlMax()) >= 0) {
            throw new BusinessRuleException(
                    "Los trigliceridos minimos deben ser menores a los maximos");
        }
    }

    private void validarExclusividad(ReglaTrigliceridosRequestDto dto, Long idExcluir) {
        List<ReglaTrigliceridos> solapadas = repository.findSolapadas(
                dto.sexo(), dto.edadMin(), dto.edadMax(),
                dto.trigliceridosMgDlMin(), dto.trigliceridosMgDlMax(), idExcluir);
        if (!solapadas.isEmpty()) {
            throw new BusinessRuleException(
                    "Existe una regla activa que se solapa en sexo, rango de edad y rango de trigliceridos");
        }
    }
}