package upeu.edu.pe.Saludabemente.recomendacionesia.core.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Snapshot de datos clínicos que se envía como contexto a la IA.
 * Se arma desde los servicios de Sebastián (Nutricional, Aptitud Física, Personal).
 */
@Getter
@Setter
@Builder
public class SnapshotDto {

    // Paciente
    private Long idPersona;
    private String nombres;
    private String sexo;
    private LocalDate fechaNacimiento;
    private Integer edad;

    // Evaluación actual
    private Long idEvaluacion;
    private LocalDate fechaEvaluacion;
    private String periodoSemestral;

    // Antropometría actual (BigDecimal como String para evitar problemas de escala)
    private String pesoKg;
    private String estaturaCm;
    private String imc;
    private String dxImc;
    private String perimetroAbdominalCm;
    private String dxPerimetroAbdominal;
    private String porcentajeGrasa;
    private String dxGrasa;
    private String porcentajeMasaMuscular;
    private String dxMasaMuscular;
    private String porcentajeGrasaVisceral;
    private String dxGrasaVisceral;

    // Bioquímica actual
    private String glucosa;
    private String colesterol;
    private String trigliceridos;
    private Integer presionSistolica;
    private Integer presionDiastolica;
    private String dxBioquimico;

    // Aptitud física actual
    private Integer abdominales30s;
    private Integer planchas30s;
    private Integer saltaSoga30s;
    private String saltoSinImpulsoCm;
    private Integer carrera400mSeg;

    // Aptitud física (flag: existe evaluación completa)
    private boolean aptitudFisicaCompleta;

    // Histórico (opcional)
    private boolean tieneHistorico;
    private String periodoSemestralAnterior;
    private LocalDate fechaEvaluacionAnterior;
    private String pesoKgAnterior;
    private String imcAnterior;
    private String dxImcAnterior;
    private String porcentajeGrasaAnterior;
    private String dxGrasaAnterior;
    private String porcentajeMasaMuscularAnterior;
    private String dxMasaMuscularAnterior;
    private String porcentajeGrasaVisceralAnterior;
    private String dxGrasaVisceralAnterior;
    private String glucosaAnterior;
    private String colesterolAnterior;
    private String trigliceridosAnterior;
    private Integer presionSistolicaAnterior;
    private Integer presionDiastolicaAnterior;
    private String dxBioquimicoAnterior;
    private Integer abdominales30sAnterior;
    private Integer planchas30sAnterior;
    private Integer saltaSoga30sAnterior;
    private String saltoSinImpulsoCmAnterior;
    private Integer carrera400mSegAnterior;
}