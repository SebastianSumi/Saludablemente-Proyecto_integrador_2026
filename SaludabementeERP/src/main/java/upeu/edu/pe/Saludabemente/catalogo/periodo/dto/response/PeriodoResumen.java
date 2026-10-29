package upeu.edu.pe.Saludabemente.catalogo.periodo.dto.response;

import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.EstadoPeriodo;

public record PeriodoResumen(
        Long id,
        String codigo,
        String descripcion,
        EstadoPeriodo estado
) {
}
