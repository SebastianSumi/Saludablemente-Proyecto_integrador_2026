package upeu.edu.pe.Saludabemente.metas.meta.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CheckInRequestDTO {

    @NotNull
    private Long idMeta;

    @NotNull
    private Long idPersona;

    /** Si es null, el service usa LocalDate.now(). */
    private LocalDate fecha;

    /** Requerido para metas CUANTITATIVAS. */
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal valorReportado;

    /** Requerido para metas CUALITATIVAS. */
    private Boolean logrado;

    @Size(max = 300)
    private String nota;

    @Size(max = 300)
    private String evidenciaUrl;
}