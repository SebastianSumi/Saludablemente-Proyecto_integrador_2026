package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.service;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaColesterolRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaColesterolResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaColesterol;

import java.util.List;

public interface ReglaColesterolService {

    List<ReglaColesterolResponseDto> listar(EstadoRegla estado, SexoBiologico sexo);

    ReglaColesterolResponseDto obtener(Long id);

    ReglaColesterolResponseDto crear(ReglaColesterolRequestDto requestDto);

    ReglaColesterolResponseDto actualizar(Long id, ReglaColesterolRequestDto requestDto);

    void desactivar(Long id);

    ReglaColesterolResponseDto reactivar(Long id);

    List<ReglaColesterol> buscarActivasParaMotor(SexoBiologico sexo, Integer edad);
}