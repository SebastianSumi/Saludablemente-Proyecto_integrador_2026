package upeu.edu.pe.Saludabemente.catalogo.periodo.dto.response;

import lombok.Getter;
import lombok.Setter;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.EstadoPeriodo;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class PeriodoResponse{
        Long id;
        String codigo;
        String descripcion;
        LocalDate fechaInicio;
        LocalDate fechaFin;
        EstadoPeriodo estado;
        LocalDateTime cerradoEn;
        String cerradoPor;

}
