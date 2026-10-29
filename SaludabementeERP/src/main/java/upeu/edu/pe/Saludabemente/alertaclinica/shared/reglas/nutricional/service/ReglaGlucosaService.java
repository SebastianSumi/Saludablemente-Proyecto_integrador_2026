package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.service;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaGlucosaRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaGlucosaResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaGlucosa;

import java.util.List;

public interface ReglaGlucosaService {

    List<ReglaGlucosaResponseDto> listar(EstadoRegla estado, SexoBiologico sexo);

    ReglaGlucosaResponseDto obtener(Long id);

    ReglaGlucosaResponseDto crear(ReglaGlucosaRequestDto requestDto);

    ReglaGlucosaResponseDto actualizar(Long id, ReglaGlucosaRequestDto requestDto);

    void desactivar(Long id);

    ReglaGlucosaResponseDto reactivar(Long id);

    List<ReglaGlucosa> buscarActivasParaMotor(SexoBiologico sexo, Integer edad);
}