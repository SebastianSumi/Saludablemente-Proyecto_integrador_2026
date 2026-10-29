package upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto;

import lombok.Getter;

@Getter
public class AlertaAgregado {

    private final long totalAlertas;
    private final long totalCriticas;
    private final long totalAtendidas;

    /**
     * Constructor usado por JPQL en SELECT new.
     * Los tres valores se calculan con COUNT y SUM CASE en la consulta.
     */
    public AlertaAgregado(long totalAlertas, long totalCriticas, long totalAtendidas) {
        this.totalAlertas = totalAlertas;
        this.totalCriticas = totalCriticas;
        this.totalAtendidas = totalAtendidas;
    }
}