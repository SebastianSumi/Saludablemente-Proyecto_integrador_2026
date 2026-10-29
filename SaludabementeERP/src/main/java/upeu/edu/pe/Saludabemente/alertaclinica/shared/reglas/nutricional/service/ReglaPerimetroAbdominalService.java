package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.service;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaPerimetroAbdominalRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaPerimetroAbdominalResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaPerimetroAbdominal;

import java.util.List;

public interface ReglaPerimetroAbdominalService {

    List<ReglaPerimetroAbdominalResponseDto> listar(EstadoRegla estado, SexoBiologico sexo);

    ReglaPerimetroAbdominalResponseDto obtener(Long id);

    ReglaPerimetroAbdominalResponseDto crear(ReglaPerimetroAbdominalRequestDto requestDto);

    ReglaPerimetroAbdominalResponseDto actualizar(Long id, ReglaPerimetroAbdominalRequestDto requestDto);

    void desactivar(Long id);

    ReglaPerimetroAbdominalResponseDto reactivar(Long id);

    List<ReglaPerimetroAbdominal> buscarActivasParaMotor(SexoBiologico sexo, Integer edad);
}