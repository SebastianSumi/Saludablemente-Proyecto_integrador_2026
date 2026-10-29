package upeu.edu.pe.Saludabemente.metas.meta.service;

import upeu.edu.pe.Saludabemente.metas.meta.dto.MetaClinicaRequestDTO;
import upeu.edu.pe.Saludabemente.metas.meta.dto.MetaRequestDTO;
import upeu.edu.pe.Saludabemente.metas.meta.dto.MetaResponseDTO;
import upeu.edu.pe.Saludabemente.metas.meta.dto.ProgresoMetaResponseDTO;
import upeu.edu.pe.Saludabemente.metas.shared.enums.EstadoMeta;
import upeu.edu.pe.Saludabemente.metas.meta.dto.MetaSugeridaRequestDTO;

// ... dentro de la interfaz, al final:


import java.util.List;

public interface MetaService {

    MetaResponseDTO crearPersonal(MetaRequestDTO dto, String usuario);

    MetaResponseDTO crearClinica(MetaClinicaRequestDTO dto, String usuario);

    MetaResponseDTO obtener(Long idMeta);

    List<MetaResponseDTO> listarPorPersona(Long idPersona, EstadoMeta estado);

    ProgresoMetaResponseDTO obtenerProgreso(Long idMeta);

    void cambiarEstado(Long idMeta, EstadoMeta nuevoEstado, String usuario);

    MetaResponseDTO crearDesdeSugerencia(MetaSugeridaRequestDTO dto, String usuario);
}