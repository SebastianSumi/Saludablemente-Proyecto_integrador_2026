package upeu.edu.pe.Saludabemente.metas.meta.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import upeu.edu.pe.Saludabemente.metas.shared.enums.PilarBienestar;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload simplificado para crear una meta aceptada desde Recomendaciones IA.
 *
 * El frontend solo conoce estos campos; el backend completa modalidad,
 * tipoCalculo, fechaInicio y tipoOrigen con valores por defecto coherentes.
 */
@Getter
@Setter
public class MetaSugeridaRequestDTO {

    @NotNull
    private Long idPersona;

    @NotBlank
    @Size(max = 60)
    private String tipoMeta;

    @NotBlank
    @Size(max = 200)
    private String descripcion;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal valorInicial;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal valorObjetivo;

    @NotNull
    private LocalDate fechaLimite;

    @NotNull
    private PilarBienestar pilar;

    @AssertTrue(message = "valorObjetivo debe ser distinto de valorInicial")
    public boolean isObjetivoDistintoDelInicial() {
        if (valorInicial == null || valorObjetivo == null) return true;
        return valorObjetivo.compareTo(valorInicial) != 0;
    }
}