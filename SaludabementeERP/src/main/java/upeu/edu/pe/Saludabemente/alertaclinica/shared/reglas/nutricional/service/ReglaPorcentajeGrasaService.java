package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.service;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaPorcentajeGrasaRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaPorcentajeGrasaResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaPorcentajeGrasa;

import java.util.List;

public interface ReglaPorcentajeGrasaService {

    List<ReglaPorcentajeGrasaResponseDto> listar(EstadoRegla estado, SexoBiologico sexo);

    ReglaPorcentajeGrasaResponseDto obtener(Long id);

    ReglaPorcentajeGrasaResponseDto crear(ReglaPorcentajeGrasaRequestDto requestDto);

    ReglaPorcentajeGrasaResponseDto actualizar(Long id, ReglaPorcentajeGrasaRequestDto requestDto);

    void desactivar(Long id);

    ReglaPorcentajeGrasaResponseDto reactivar(Long id);

    List<ReglaPorcentajeGrasa> buscarActivasParaMotor(SexoBiologico sexo, Integer edad);
}