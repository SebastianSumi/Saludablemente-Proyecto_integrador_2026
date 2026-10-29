package upeu.edu.pe.Saludabemente.metas.shared.enums;

/**
 * Como se acumula el avance al recibir check-ins.
 * ULTIMO_VALOR: cada check-in reemplaza el valor actual (ej. bajar a 70 kg).
 * ACUMULADO:    cada check-in suma al valor actual (ej. acumular 60 L de agua).
 */
public enum TipoCalculo {
    ULTIMO_VALOR,
    ACUMULADO
}