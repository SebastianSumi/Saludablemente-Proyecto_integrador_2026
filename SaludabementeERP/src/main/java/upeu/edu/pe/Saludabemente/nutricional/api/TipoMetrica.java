package upeu.edu.pe.Saludabemente.nutricional.api;

/**
 * Vocabulario cerrado de tipos de métrica.
 * Debe coincidir exactamente con el catálogo que espera el motor de Alertas.
 */
public final class TipoMetrica {

    private TipoMetrica() {
        throw new UnsupportedOperationException("Clase de constantes");
    }

    public static final String IMC = "IMC";
    public static final String PERIMETRO_ABDOMINAL = "PERIMETRO_ABDOMINAL";
    public static final String PORCENTAJE_GRASA = "PORCENTAJE_GRASA";
    public static final String MASA_MUSCULAR = "MASA_MUSCULAR";
    public static final String GRASA_VISCERAL = "GRASA_VISCERAL";
    public static final String GLUCOSA = "GLUCOSA";
    public static final String COLESTEROL = "COLESTEROL";
    public static final String TRIGLICERIDOS = "TRIGLICERIDOS";
    public static final String PRESION_SISTOLICA = "PRESION_SISTOLICA";
    public static final String PRESION_DIASTOLICA = "PRESION_DIASTOLICA";
}