package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaPresionArterialRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaPresionArterialResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaPresionArterial;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.mapper.ReglaPresionArterialMapper;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.repository.ReglaPresionArterialRepository;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReglaPresionArterialServiceImpl implements ReglaPresionArterialService {

    private final ReglaPresionArterialRepository repository;
    private final ReglaPresionArterialMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ReglaPresionArterialResponseDto> listar(EstadoRegla estado, SexoBiologico sexo) {
        List<ReglaPresionArterial> reglas;
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
    public ReglaPresionArterialResponseDto obtener(Long id) {
        return mapper.toResponseDto(buscarOFallar(id));
    }

    @Override
    @Transactional
    public ReglaPresionArterialResponseDto crear(ReglaPresionArterialRequestDto requestDto) {
        validarRangoInterno(requestDto);
        validarCoherenciaClinica(requestDto);
        validarExclusividad(requestDto, null);

        ReglaPresionArterial entidad = mapper.toEntity(requestDto);
        entidad.setEstado(EstadoRegla.ACTIVO);
        return mapper.toResponseDto(repository.save(entidad));
    }

    @Override
    @Transactional
    public ReglaPresionArterialResponseDto actualizar(Long id, ReglaPresionArterialRequestDto requestDto) {
        ReglaPresionArterial entidad = buscarOFallar(id);
        validarRangoInterno(requestDto);
        validarCoherenciaClinica(requestDto);
        validarExclusividad(requestDto, id);

        mapper.updateEntityFromDto(requestDto, entidad);
        return mapper.toResponseDto(repository.save(entidad));
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        ReglaPresionArterial entidad = buscarOFallar(id);
        entidad.setEstado(EstadoRegla.INACTIVO);
        repository.save(entidad);
    }

    @Override
    @Transactional
    public ReglaPresionArterialResponseDto reactivar(Long id) {
        ReglaPresionArterial entidad = buscarOFallar(id);
        entidad.setEstado(EstadoRegla.ACTIVO);
        entidad.setFechaBaja(null);
        return mapper.toResponseDto(repository.save(entidad));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReglaPresionArterial> buscarActivasParaMotor(SexoBiologico sexo, Integer edad) {
        return repository.findReglasActivasPorDemografia(sexo, edad);
    }

    private ReglaPresionArterial buscarOFallar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ReglaPresionArterial no encontrada: " + id));
    }

    private void validarRangoInterno(ReglaPresionArterialRequestDto dto) {
        if (dto.edadMin() >= dto.edadMax()) {
            throw new BusinessRuleException(
                    "La edad minima debe ser menor a la edad maxima");
        }
        if (dto.sistolicaMmHgMin() >= dto.sistolicaMmHgMax()) {
            throw new BusinessRuleException(
                    "La sistolica minima debe ser menor a la sistolica maxima");
        }
        if (dto.diastolicaMmHgMin() >= dto.diastolicaMmHgMax()) {
            throw new BusinessRuleException(
                    "La diastolica minima debe ser menor a la diastolica maxima");
        }
    }

    private void validarCoherenciaClinica(ReglaPresionArterialRequestDto dto) {
        if (dto.sistolicaMmHgMin() <= dto.diastolicaMmHgMin()) {
            throw new BusinessRuleException(
                    "La sistolica minima debe ser mayor a la diastolica minima");
        }
        if (dto.sistolicaMmHgMax() <= dto.diastolicaMmHgMax()) {
            throw new BusinessRuleException(
                    "La sistolica maxima debe ser mayor a la diastolica maxima");
        }
    }

    private void validarExclusividad(ReglaPresionArterialRequestDto dto, Long idExcluir) {
        List<ReglaPresionArterial> solapadas = repository.findSolapadas(
                dto.sexo(), dto.edadMin(), dto.edadMax(),
                dto.sistolicaMmHgMin(), dto.sistolicaMmHgMax(),
                dto.diastolicaMmHgMin(), dto.diastolicaMmHgMax(),
                idExcluir);
        if (!solapadas.isEmpty()) {
            throw new BusinessRuleException(
                    "Existe una regla activa que se solapa en sexo, rango de edad, presion sistolica y presion diastolica");
        }
    }
}