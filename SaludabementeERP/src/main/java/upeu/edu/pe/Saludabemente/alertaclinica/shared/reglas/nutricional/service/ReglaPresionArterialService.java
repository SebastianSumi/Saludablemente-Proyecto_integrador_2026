package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.service;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaPresionArterialRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaPresionArterialResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaPresionArterial;

import java.util.List;

public interface ReglaPresionArterialService {

    List<ReglaPresionArterialResponseDto> listar(EstadoRegla estado, SexoBiologico sexo);

    ReglaPresionArterialResponseDto obtener(Long id);

    ReglaPresionArterialResponseDto crear(ReglaPresionArterialRequestDto requestDto);

    ReglaPresionArterialResponseDto actualizar(Long id, ReglaPresionArterialRequestDto requestDto);

    void desactivar(Long id);

    ReglaPresionArterialResponseDto reactivar(Long id);

    List<ReglaPresionArterial> buscarActivasParaMotor(SexoBiologico sexo, Integer edad);
}