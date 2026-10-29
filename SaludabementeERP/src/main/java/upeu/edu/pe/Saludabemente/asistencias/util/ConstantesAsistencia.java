package upeu.edu.pe.Saludabemente.asistencias.util;

public final class ConstantesAsistencia {

    private ConstantesAsistencia() {}

    public static final int TOLERANCIA_GEOCERCA_DEFAULT_METROS = 100;
    public static final double PORCENTAJE_TIEMPO_EFECTIVO_MINIMO = 0.80;
    public static final int QR_ROTACION_SEGUNDOS_DEFAULT = 180;
    public static final int QR_TOLERANCIA_RELOJ_SEGUNDOS_DEFAULT = 90;
    public static final int QR_VENTANA_ANTICIPACION_MIN_DEFAULT = 15;
    public static final int QR_VENTANA_CIERRE_MIN_DEFAULT = 15;
    public static final String USUARIO_SISTEMA = "SYSTEM";

    // Estados de actividad
    public static final String ACTIVIDAD_EN_CURSO = "EN_CURSO";
    public static final String ACTIVIDAD_FINALIZADA = "FINALIZADA";
    public static final String ACTIVIDAD_CANCELADA = "CANCELADA";
    public static final String ACTIVIDAD_PROGRAMADA = "PROGRAMADA";
}