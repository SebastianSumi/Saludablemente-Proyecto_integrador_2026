package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.service;

import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaSaltoSinImpulsoRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaSaltoSinImpulsoResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.entity.ReglaSaltoSinImpulso;

import java.util.List;

public interface ReglaSaltoSinImpulsoService {

    List<ReglaSaltoSinImpulsoResponseDto> listar(EstadoRegla estado, SexoBiologico sexo);

    ReglaSaltoSinImpulsoResponseDto obtener(Long id);

    ReglaSaltoSinImpulsoResponseDto crear(ReglaSaltoSinImpulsoRequestDto requestDto);

    ReglaSaltoSinImpulsoResponseDto actualizar(Long id, ReglaSaltoSinImpulsoRequestDto requestDto);

    void desactivar(Long id);

    ReglaSaltoSinImpulsoResponseDto reactivar(Long id);

    List<ReglaSaltoSinImpulso> buscarActivasParaMotor(SexoBiologico sexo, Integer edad);
}