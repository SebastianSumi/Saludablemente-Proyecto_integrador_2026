package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.service;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaAbdominalesRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaAbdominalesResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.entity.ReglaAbdominales;

import java.util.List;

public interface ReglaAbdominalesService {

    List<ReglaAbdominalesResponseDto> listar(EstadoRegla estado, SexoBiologico sexo);

    ReglaAbdominalesResponseDto obtener(Long id);

    ReglaAbdominalesResponseDto crear(ReglaAbdominalesRequestDto requestDto);

    ReglaAbdominalesResponseDto actualizar(Long id, ReglaAbdominalesRequestDto requestDto);

    void desactivar(Long id);

    ReglaAbdominalesResponseDto reactivar(Long id);

    List<ReglaAbdominales> buscarActivasParaMotor(SexoBiologico sexo, Integer edad);
}