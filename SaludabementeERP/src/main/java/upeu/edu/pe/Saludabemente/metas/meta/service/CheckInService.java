package upeu.edu.pe.Saludabemente.metas.meta.service;

import upeu.edu.pe.Saludabemente.metas.meta.dto.CheckInRequestDTO;
import upeu.edu.pe.Saludabemente.metas.meta.dto.ProgresoMetaResponseDTO;

import java.util.List;

public interface CheckInService {

    ProgresoMetaResponseDTO registrar(CheckInRequestDTO dto, String usuario);

    List<ProgresoMetaResponseDTO> historial(Long idMeta);
}