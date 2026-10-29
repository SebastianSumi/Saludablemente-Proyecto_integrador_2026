package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.service;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaSaltaSogaRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaSaltaSogaResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.entity.ReglaSaltaSoga;

import java.util.List;

public interface ReglaSaltaSogaService {

    List<ReglaSaltaSogaResponseDto> listar(EstadoRegla estado, SexoBiologico sexo);

    ReglaSaltaSogaResponseDto obtener(Long id);

    ReglaSaltaSogaResponseDto crear(ReglaSaltaSogaRequestDto requestDto);

    ReglaSaltaSogaResponseDto actualizar(Long id, ReglaSaltaSogaRequestDto requestDto);

    void desactivar(Long id);

    ReglaSaltaSogaResponseDto reactivar(Long id);

    List<ReglaSaltaSoga> buscarActivasParaMotor(SexoBiologico sexo, Integer edad);
}