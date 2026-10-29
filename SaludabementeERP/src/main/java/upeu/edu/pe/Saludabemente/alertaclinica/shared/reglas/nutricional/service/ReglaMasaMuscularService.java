package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.service;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaMasaMuscularRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaMasaMuscularResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaMasaMuscular;

import java.util.List;

public interface ReglaMasaMuscularService {

    List<ReglaMasaMuscularResponseDto> listar(EstadoRegla estado, SexoBiologico sexo);

    ReglaMasaMuscularResponseDto obtener(Long id);

    ReglaMasaMuscularResponseDto crear(ReglaMasaMuscularRequestDto requestDto);

    ReglaMasaMuscularResponseDto actualizar(Long id, ReglaMasaMuscularRequestDto requestDto);

    void desactivar(Long id);

    ReglaMasaMuscularResponseDto reactivar(Long id);

    List<ReglaMasaMuscular> buscarActivasParaMotor(SexoBiologico sexo, Integer edad);
}