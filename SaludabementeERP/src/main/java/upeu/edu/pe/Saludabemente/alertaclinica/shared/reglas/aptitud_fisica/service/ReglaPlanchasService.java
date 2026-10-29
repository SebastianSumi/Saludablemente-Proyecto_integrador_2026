package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.service;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaPlanchasRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaPlanchasResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.entity.ReglaPlanchas;

import java.util.List;

public interface ReglaPlanchasService {

    List<ReglaPlanchasResponseDto> listar(EstadoRegla estado, SexoBiologico sexo);

    ReglaPlanchasResponseDto obtener(Long id);

    ReglaPlanchasResponseDto crear(ReglaPlanchasRequestDto requestDto);

    ReglaPlanchasResponseDto actualizar(Long id, ReglaPlanchasRequestDto requestDto);

    void desactivar(Long id);

    ReglaPlanchasResponseDto reactivar(Long id);

    List<ReglaPlanchas> buscarActivasParaMotor(SexoBiologico sexo, Integer edad);
}