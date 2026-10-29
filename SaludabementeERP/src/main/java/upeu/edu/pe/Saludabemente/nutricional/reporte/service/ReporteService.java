package upeu.edu.pe.Saludabemente.nutricional.reporte.service;

/**
 * Generación de entregables documentales del módulo Nutricional.
 * Cubre las funciones 17 y 18 de la sección de reportes de PLANIFICACION.docx.
 */
public interface ReporteService {

    /**
     * Excel consolidado del semestre: una fila por evaluación con
     * snapshot + IMC + diagnósticos. Columnas: DNI, Nombres, Área,
     * Periodo, Fecha, Estado, IMC, Dx IMC, Dx Bioquímico.
     */
    byte[] exportarConsolidadoSemestral(String periodo);

    /**
     * Ficha clínica individual en PDF con todos los datos de la evaluación.
     */
    byte[] generarFichaClinicaPdf(Long idEvaluacion);
}