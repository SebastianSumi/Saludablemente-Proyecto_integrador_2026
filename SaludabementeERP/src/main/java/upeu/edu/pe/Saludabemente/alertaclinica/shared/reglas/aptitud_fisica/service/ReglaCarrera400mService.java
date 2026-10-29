package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.service;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaCarrera400mRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaCarrera400mResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.entity.ReglaCarrera400m;

import java.util.List;

public interface ReglaCarrera400mService {

    List<ReglaCarrera400mResponseDto> listar(EstadoRegla estado, SexoBiologico sexo);

    ReglaCarrera400mResponseDto obtener(Long id);

    ReglaCarrera400mResponseDto crear(ReglaCarrera400mRequestDto requestDto);

    ReglaCarrera400mResponseDto actualizar(Long id, ReglaCarrera400mRequestDto requestDto);

    void desactivar(Long id);

    ReglaCarrera400mResponseDto reactivar(Long id);

    List<ReglaCarrera400m> buscarActivasParaMotor(SexoBiologico sexo, Integer edad);
}