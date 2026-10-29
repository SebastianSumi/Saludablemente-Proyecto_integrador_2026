package upeu.edu.pe.Saludabemente.metas.meta.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import upeu.edu.pe.Saludabemente.metas.shared.enums.ModalidadMeta;
import upeu.edu.pe.Saludabemente.metas.shared.enums.PilarBienestar;
import upeu.edu.pe.Saludabemente.metas.shared.enums.TipoCalculo;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class MetaRequestDTO {

    @NotNull
    private Long idPersona;

    @NotNull
    private PilarBienestar pilar;

    @NotNull
    private ModalidadMeta modalidad;

    @NotNull
    private TipoCalculo tipoCalculo;

    @NotBlank
    @Size(max = 200)
    private String descripcion;

    @NotBlank
    @Size(max = 60)
    private String indicador;

    @Size(max = 20)
    private String unidad;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal valorInicial;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal valorObjetivo;

    @NotNull
    private LocalDate fechaInicio;

    @NotNull
    private LocalDate fechaObjetivo;

    @AssertTrue(message = "fechaObjetivo debe ser estrictamente posterior a fechaInicio")
    public boolean isRangoFechasValido() {
        return fechaInicio == null || fechaObjetivo == null
                || fechaObjetivo.isAfter(fechaInicio);
    }
}