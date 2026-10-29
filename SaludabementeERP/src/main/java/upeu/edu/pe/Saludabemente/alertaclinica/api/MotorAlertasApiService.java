package upeu.edu.pe.Saludabemente.alertaclinica.api;


import upeu.edu.pe.Saludabemente.aptitudfisica.api.EvaluacionFisicaIngresoDto;
import upeu.edu.pe.Saludabemente.nutricional.api.EvaluacionNutricionalIngresoDto;


/**
 * API publica del motor de alertas clinicas.
 *
 * Otros modulos (Nutricional, Aptitud Fisica) llaman a estos metodos
 * cuando terminan de registrar una evaluacion completa.
 *
 * El metodo corre en su propia transaccion (REQUIRES_NEW), por lo que
 * un fallo del motor no revierte la evaluacion ya persistida por el
 * modulo que llama.
 *
 * El metodo no devuelve nada: quien llama no sabe si se genero alerta
 * ni cuanto puntaje acumulo. Es responsabilidad exclusiva del motor.
 */
public interface MotorAlertasApiService {

    /**
     * Procesa una evaluacion nutricional completa y, si corresponde,
     * genera una alerta clinica.
     *
     * @param dto payload ensamblado por el modulo Nutricional
     */
    void procesarNutricional(EvaluacionNutricionalIngresoDto dto);

    /**
     * Procesa una evaluacion de aptitud fisica completa y, si corresponde,
     * genera una alerta clinica.
     *
     * @param dto payload ensamblado por el modulo Aptitud Fisica
     */
    void procesarFisica(EvaluacionFisicaIngresoDto dto);
}