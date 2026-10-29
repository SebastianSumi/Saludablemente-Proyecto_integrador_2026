package upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AlertaReporte {

    private final AlertaAgregado agregado;
    private final List<AlertaClinicaResumen> alertas;
}