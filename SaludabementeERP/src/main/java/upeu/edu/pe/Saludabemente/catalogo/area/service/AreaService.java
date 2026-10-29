package upeu.edu.pe.Saludabemente.catalogo.area.service;

import org.springframework.data.domain.Sort;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.request.AreaRequest;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResponse;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResumen;
import upeu.edu.pe.Saludabemente.catalogo.area.entity.AreaTrabajo;
import upeu.edu.pe.Saludabemente.personal.api.PersonaNoticiasDTO;

import java.util.List;
import java.util.Optional;

public interface AreaService {

    // --- Uso interno (controller) ---
    List<AreaResponse> buscar(Boolean activo, Sort sort);
    AreaResponse obtener(Long id);
    AreaResponse crear(AreaRequest request);
    AreaResponse actualizar(Long id, AreaRequest request);
    void inactivar(Long id);

    // --- Servicios públicos para otros módulos ---
    AreaTrabajo buscarOFallar(Long id);
    List<AreaResumen> listarResumen();
    Optional<AreaResumen> obtenerAreaResumen(Long idArea);
}