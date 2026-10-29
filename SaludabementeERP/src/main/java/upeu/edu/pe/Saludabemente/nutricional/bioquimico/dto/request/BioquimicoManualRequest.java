package upeu.edu.pe.Saludabemente.nutricional.bioquimico.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BioquimicoManualRequest {

    @DecimalMin(value = "0.0", message = "La glucosa no puede ser negativa")
    private BigDecimal glucosa;

    @DecimalMin(value = "0.0", message = "El colesterol no puede ser negativo")
    private BigDecimal colesterol;

    @DecimalMin(value = "0.0", message = "Los triglicéridos no pueden ser negativos")
    private BigDecimal trigliceridos;

    @Min(value = 50, message = "La presión sistólica parece fuera de rango")
    @Max(value = 250, message = "La presión sistólica parece fuera de rango")
    private Integer presionSistolica;

    @Min(value = 30, message = "La presión diastólica parece fuera de rango")
    @Max(value = 150, message = "La presión diastólica parece fuera de rango")
    private Integer presionDiastolica;
}