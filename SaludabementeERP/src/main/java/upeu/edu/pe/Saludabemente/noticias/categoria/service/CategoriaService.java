package upeu.edu.pe.Saludabemente.noticias.categoria.service;

import upeu.edu.pe.Saludabemente.noticias.categoria.dto.CategoriaRequestDTO;
import upeu.edu.pe.Saludabemente.noticias.categoria.dto.CategoriaResponseDTO;

import java.util.List;

public interface CategoriaService {

    List<CategoriaResponseDTO> listarActivas();

    List<CategoriaResponseDTO> listarTodas();

    CategoriaResponseDTO obtener(Long id);

    CategoriaResponseDTO crear(CategoriaRequestDTO request);

    CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO request);

    CategoriaResponseDTO activar(Long id);

    CategoriaResponseDTO inactivar(Long id);

    void eliminar(Long id);
}