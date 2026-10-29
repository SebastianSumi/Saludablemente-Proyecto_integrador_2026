package upeu.edu.pe.Saludabemente.catalogo.periodo.service;

import org.springframework.data.domain.Sort;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.EstadoPeriodo;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.request.PeriodoRequest;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.response.PeriodoResponse;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.response.PeriodoResumen;
import upeu.edu.pe.Saludabemente.catalogo.periodo.entity.Periodo;

import java.util.List;

public interface PeriodoService {

    // --- Uso interno (controller) ---
    List<PeriodoResponse> buscar(EstadoPeriodo estado, Sort sort);
    PeriodoResponse obtener(Long id);
    PeriodoResponse crear(PeriodoRequest request);
    PeriodoResponse actualizar(Long id, PeriodoRequest request);
    PeriodoResponse marcarVigente(Long id);
    PeriodoResponse cerrar(Long id);
    void eliminar(Long id);

    // --- Servicios publicos para otros modulos ---
    PeriodoResponse obtenerVigente();
    PeriodoResponse obtenerPorCodigo(String codigo);
    Periodo buscarOFallar(Long id);
    List<PeriodoResumen> listarResumen();
}