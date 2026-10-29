package upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaClinicaDetalleResponse {

    private Long idDetalle;
    private String tipoIndicador;
    private BigDecimal valorMedido;
    private BigDecimal valorMin;
    private BigDecimal valorMax;
    private String clasificacion;
    private Integer puntosRiesgo;
    private String mensaje;
    private String motorDetonante;
}