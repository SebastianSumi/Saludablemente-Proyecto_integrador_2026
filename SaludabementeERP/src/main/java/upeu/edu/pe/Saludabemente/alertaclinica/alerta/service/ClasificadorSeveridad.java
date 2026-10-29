package upeu.edu.pe.Saludabemente.alertaclinica.alerta.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.SeveridadAlerta;

@Component
public class ClasificadorSeveridad {

    @Value("${saludablemente.alertas.severidad.leve-max:3}")
    private int leveMax;

    @Value("${saludablemente.alertas.severidad.moderada-max:7}")
    private int moderadaMax;

    /**
     * Clasifica el puntaje total de una alerta segun los umbrales
     * configurados en application.yml.
     *
     * Regla:
     *   puntaje <= leveMax       -> LEVE
     *   puntaje <= moderadaMax   -> MODERADA
     *   puntaje >  moderadaMax   -> CRITICA
     */
    public SeveridadAlerta clasificar(int puntajeTotal) {
        if (puntajeTotal <= leveMax) {
            return SeveridadAlerta.LEVE;
        }
        if (puntajeTotal <= moderadaMax) {
            return SeveridadAlerta.MODERADA;
        }
        return SeveridadAlerta.CRITICA;
    }
}