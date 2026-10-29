package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.service;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaGrasaVisceralRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaGrasaVisceralResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaGrasaVisceral;

import java.util.List;

public interface ReglaGrasaVisceralService {

    List<ReglaGrasaVisceralResponseDto> listar(EstadoRegla estado, SexoBiologico sexo);

    ReglaGrasaVisceralResponseDto obtener(Long id);

    ReglaGrasaVisceralResponseDto crear(ReglaGrasaVisceralRequestDto requestDto);

    ReglaGrasaVisceralResponseDto actualizar(Long id, ReglaGrasaVisceralRequestDto requestDto);

    void desactivar(Long id);

    ReglaGrasaVisceralResponseDto reactivar(Long id);

    List<ReglaGrasaVisceral> buscarActivasParaMotor(SexoBiologico sexo, Integer edad);
}