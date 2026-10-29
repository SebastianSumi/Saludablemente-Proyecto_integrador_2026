package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.service;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaImcRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaImcResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaImc;

import java.util.List;

public interface ReglaImcService {

    List<ReglaImcResponseDto> listar(EstadoRegla estado, SexoBiologico sexo);

    ReglaImcResponseDto obtener(Long id);

    ReglaImcResponseDto crear(ReglaImcRequestDto requestDto);

    ReglaImcResponseDto actualizar(Long id, ReglaImcRequestDto requestDto);

    void desactivar(Long id);

    ReglaImcResponseDto reactivar(Long id);

    List<ReglaImc> buscarActivasParaMotor(SexoBiologico sexo, Integer edad);
}