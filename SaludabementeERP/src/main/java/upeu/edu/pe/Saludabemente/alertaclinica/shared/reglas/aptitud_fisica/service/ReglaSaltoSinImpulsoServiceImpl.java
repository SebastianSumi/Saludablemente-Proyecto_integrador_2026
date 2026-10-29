package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaSaltoSinImpulsoRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaSaltoSinImpulsoResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.entity.ReglaSaltoSinImpulso;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.mapper.ReglaSaltoSinImpulsoMapper;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.repository.ReglaSaltoSinImpulsoRepository;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReglaSaltoSinImpulsoServiceImpl implements ReglaSaltoSinImpulsoService {

    private final ReglaSaltoSinImpulsoRepository repository;
    private final ReglaSaltoSinImpulsoMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ReglaSaltoSinImpulsoResponseDto> listar(EstadoRegla estado, SexoBiologico sexo) {
        List<ReglaSaltoSinImpulso> reglas;
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
    public ReglaSaltoSinImpulsoResponseDto obtener(Long id) {
        return mapper.toResponseDto(buscarOFallar(id));
    }

    @Override
    @Transactional
    public ReglaSaltoSinImpulsoResponseDto crear(ReglaSaltoSinImpulsoRequestDto requestDto) {
        validarRangoInterno(requestDto);
        validarExclusividad(requestDto, null);

        ReglaSaltoSinImpulso entidad = mapper.toEntity(requestDto);
        entidad.setEstado(EstadoRegla.ACTIVO);
        return mapper.toResponseDto(repository.save(entidad));
    }

    @Override
    @Transactional
    public ReglaSaltoSinImpulsoResponseDto actualizar(Long id, ReglaSaltoSinImpulsoRequestDto requestDto) {
        ReglaSaltoSinImpulso entidad = buscarOFallar(id);
        validarRangoInterno(requestDto);
        validarExclusividad(requestDto, id);

        mapper.updateEntityFromDto(requestDto, entidad);
        return mapper.toResponseDto(repository.save(entidad));
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        ReglaSaltoSinImpulso entidad = buscarOFallar(id);
        entidad.setEstado(EstadoRegla.INACTIVO);
        repository.save(entidad);
    }

    @Override
    @Transactional
    public ReglaSaltoSinImpulsoResponseDto reactivar(Long id) {
        ReglaSaltoSinImpulso entidad = buscarOFallar(id);
        entidad.setEstado(EstadoRegla.ACTIVO);
        entidad.setFechaBaja(null);
        return mapper.toResponseDto(repository.save(entidad));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReglaSaltoSinImpulso> buscarActivasParaMotor(SexoBiologico sexo, Integer edad) {
        return repository.findReglasActivasPorDemografia(sexo, edad);
    }

    private ReglaSaltoSinImpulso buscarOFallar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ReglaSaltoSinImpulso no encontrada: " + id));
    }

    private void validarRangoInterno(ReglaSaltoSinImpulsoRequestDto dto) {
        if (dto.edadMin() >= dto.edadMax()) {
            throw new BusinessRuleException(
                    "La edad minima debe ser menor a la edad maxima");
        }
        if (dto.distanciaCmMin().compareTo(dto.distanciaCmMax()) >= 0) {
            throw new BusinessRuleException(
                    "La distancia minima debe ser menor a la distancia maxima");
        }
    }

    private void validarExclusividad(ReglaSaltoSinImpulsoRequestDto dto, Long idExcluir) {
        List<ReglaSaltoSinImpulso> solapadas = repository.findSolapadas(
                dto.sexo(), dto.edadMin(), dto.edadMax(),
                dto.distanciaCmMin(), dto.distanciaCmMax(), idExcluir);
        if (!solapadas.isEmpty()) {
            throw new BusinessRuleException(
                    "Existe una regla activa que se solapa en sexo, rango de edad y rango de distancia");
        }
    }
}