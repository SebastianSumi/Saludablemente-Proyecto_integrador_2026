package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.service;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaTrigliceridosRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaTrigliceridosResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaTrigliceridos;

import java.util.List;

public interface ReglaTrigliceridosService {

    List<ReglaTrigliceridosResponseDto> listar(EstadoRegla estado, SexoBiologico sexo);

    ReglaTrigliceridosResponseDto obtener(Long id);

    ReglaTrigliceridosResponseDto crear(ReglaTrigliceridosRequestDto requestDto);

    ReglaTrigliceridosResponseDto actualizar(Long id, ReglaTrigliceridosRequestDto requestDto);

    void desactivar(Long id);

    ReglaTrigliceridosResponseDto reactivar(Long id);

    List<ReglaTrigliceridos> buscarActivasParaMotor(SexoBiologico sexo, Integer edad);
}