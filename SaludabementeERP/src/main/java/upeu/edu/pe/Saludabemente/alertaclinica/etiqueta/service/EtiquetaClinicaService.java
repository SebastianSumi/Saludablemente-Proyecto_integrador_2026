package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.service;

import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.EtiquetaClinicaRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.EtiquetaClinicaResponseDto;

import java.util.List;

public interface EtiquetaClinicaService {

    List<EtiquetaClinicaResponseDto> listar(String estado);

    EtiquetaClinicaResponseDto obtener(Long id);

    EtiquetaClinicaResponseDto crear(EtiquetaClinicaRequestDto dto);

    EtiquetaClinicaResponseDto actualizar(Long id, EtiquetaClinicaRequestDto dto);

    void desactivar(Long id);

    EtiquetaClinicaResponseDto reactivar(Long id);
}
