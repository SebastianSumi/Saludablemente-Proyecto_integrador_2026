package upeu.edu.pe.Saludabemente.recomendacionesia.core.prompt;

import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.dto.SnapshotDto;

/**
 * Ensambla el user prompt (8 bloques) a partir del snapshot del colaborador.
 */
@Component
public class PromptAssembler {

    public String armar(SnapshotDto s) {
        StringBuilder sb = new StringBuilder();

        // Bloque 1
        sb.append("A continuación se presentan los datos clínicos y de aptitud física de un ")
                .append("colaborador del programa \"Universidad Saludable\", correspondientes a su ")
                .append("evaluación semestral actual. Analiza esta información y genera el JSON con ")
                .append("los 8 nodos según la estructura definida en el system prompt y el schema adjunto.\n\n");

        // Bloque 2 - Paciente
        sb.append("## Datos del paciente\n\n");
        sb.append("- Sexo: ").append(traducirSexo(s.getSexo())).append("\n");
        sb.append("- Fecha de nacimiento: ").append(s.getFechaNacimiento()).append("\n");
        sb.append("- Edad al momento de la evaluación: ").append(s.getEdad()).append(" años\n");
        if (s.getNombres() != null && !s.getNombres().isBlank()) {
            sb.append("- Nombres: ").append(s.getNombres()).append("\n");
        }
        sb.append("\n");

        // Bloque 3 - Evaluación actual
        sb.append("## Evaluación nutricional actual\n\n");
        sb.append("Periodo: ").append(s.getPeriodoSemestral()).append("\n");
        sb.append("Fecha: ").append(s.getFechaEvaluacion()).append("\n\n");
        sb.append("### Antropometría\n\n");
        sb.append("- Peso: ").append(nvl(s.getPesoKg())).append(" kg\n");
        sb.append("- Estatura: ").append(nvl(s.getEstaturaCm())).append(" cm\n");
        sb.append("- IMC: ").append(nvl(s.getImc())).append(" (").append(nvl(s.getDxImc())).append(")\n");
        sb.append("- Perímetro abdominal: ").append(nvl(s.getPerimetroAbdominalCm()))
                .append(" cm (").append(nvl(s.getDxPerimetroAbdominal())).append(")\n");
        sb.append("- % de grasa corporal: ").append(nvl(s.getPorcentajeGrasa()))
                .append(" (").append(nvl(s.getDxGrasa())).append(")\n");
        sb.append("- % de masa muscular: ").append(nvl(s.getPorcentajeMasaMuscular()))
                .append(" (").append(nvl(s.getDxMasaMuscular())).append(")\n");
        sb.append("- % de grasa visceral: ").append(nvl(s.getPorcentajeGrasaVisceral()))
                .append(" (").append(nvl(s.getDxGrasaVisceral())).append(")\n\n");
        sb.append("### Bioquímica\n\n");
        sb.append("- Glucosa: ").append(nvl(s.getGlucosa())).append(" mg/dL\n");
        sb.append("- Colesterol: ").append(nvl(s.getColesterol())).append(" mg/dL\n");
        sb.append("- Triglicéridos: ").append(nvl(s.getTrigliceridos())).append(" mg/dL\n");
        sb.append("- Presión arterial: ").append(nvl(s.getPresionSistolica()))
                .append("/").append(nvl(s.getPresionDiastolica())).append(" mmHg\n");
        sb.append("- Diagnóstico bioquímico global: ").append(nvl(s.getDxBioquimico())).append("\n\n");

        // Bloque 4 - Aptitud física
        sb.append("## Aptitud física actual\n\n");
        if (!s.isAptitudFisicaCompleta()) {
            sb.append("No hay pruebas físicas registradas para este colaborador en la campaña ")
                    .append("actual. Devuelve recomendacion_ejercicio como null.\n\n");
        } else {
            sb.append("- Abdominales (30 s): ").append(nvl(s.getAbdominales30s())).append(" repeticiones\n");
            sb.append("- Planchas (30 s): ").append(nvl(s.getPlanchas30s())).append(" repeticiones\n");
            sb.append("- Salta soga (30 s): ").append(nvl(s.getSaltaSoga30s())).append(" repeticiones\n");
            sb.append("- Salto sin impulso: ").append(nvl(s.getSaltoSinImpulsoCm())).append(" cm\n");
            sb.append("- Carrera 400 m: ").append(nvl(s.getCarrera400mSeg())).append(" segundos\n\n");
        }

        // Bloque 5 - Histórico (condicional)
        if (s.isTieneHistorico()) {
            sb.append("## Evaluación nutricional del semestre anterior\n\n");
            sb.append("Periodo: ").append(s.getPeriodoSemestralAnterior()).append("\n");
            sb.append("Fecha: ").append(s.getFechaEvaluacionAnterior()).append("\n\n");
            sb.append("### Antropometría (semestre anterior)\n\n");
            sb.append("- Peso: ").append(nvl(s.getPesoKgAnterior())).append(" kg\n");
            sb.append("- IMC: ").append(nvl(s.getImcAnterior())).append(" (").append(nvl(s.getDxImcAnterior())).append(")\n");
            sb.append("- % grasa corporal: ").append(nvl(s.getPorcentajeGrasaAnterior()))
                    .append(" (").append(nvl(s.getDxGrasaAnterior())).append(")\n");
            sb.append("- % masa muscular: ").append(nvl(s.getPorcentajeMasaMuscularAnterior()))
                    .append(" (").append(nvl(s.getDxMasaMuscularAnterior())).append(")\n");
            sb.append("- % grasa visceral: ").append(nvl(s.getPorcentajeGrasaVisceralAnterior()))
                    .append(" (").append(nvl(s.getDxGrasaVisceralAnterior())).append(")\n\n");
            sb.append("### Bioquímica (semestre anterior)\n\n");
            sb.append("- Glucosa: ").append(nvl(s.getGlucosaAnterior())).append(" mg/dL\n");
            sb.append("- Colesterol: ").append(nvl(s.getColesterolAnterior())).append(" mg/dL\n");
            sb.append("- Triglicéridos: ").append(nvl(s.getTrigliceridosAnterior())).append(" mg/dL\n");
            sb.append("- Presión arterial: ").append(nvl(s.getPresionSistolicaAnterior()))
                    .append("/").append(nvl(s.getPresionDiastolicaAnterior())).append(" mmHg\n\n");
            sb.append("### Aptitud física (semestre anterior)\n\n");
            sb.append("- Abdominales (30 s): ").append(nvl(s.getAbdominales30sAnterior())).append(" repeticiones\n");
            sb.append("- Planchas (30 s): ").append(nvl(s.getPlanchas30sAnterior())).append(" repeticiones\n");
            sb.append("- Salta soga (30 s): ").append(nvl(s.getSaltaSoga30sAnterior())).append(" repeticiones\n");
            sb.append("- Salto sin impulso: ").append(nvl(s.getSaltoSinImpulsoCmAnterior())).append(" cm\n");
            sb.append("- Carrera 400 m: ").append(nvl(s.getCarrera400mSegAnterior())).append(" segundos\n\n");
        }

        // Bloque 7 - Reglas de negocio
        sb.append("## Reglas de negocio del programa\n\n");
        sb.append("- No recetar ejercicios de alto impacto si el IMC es mayor a 30.\n");
        sb.append("- Toda meta debe seguir formato SMART con fecha límite en el próximo semestre.\n");
        sb.append("- Horizonte temporal de todas las recomendaciones: 6 meses.\n");
        sb.append("- Pilares válidos: NUTRICION, ACTIVIDAD, DESCANSO, ELECCION, MEDIO_AMBIENTE, ")
                .append("CONFIANZA, RELACIONES_INTERPERSONALES, PERSPECTIVA.\n");
        sb.append("- Cuando no tengas certeza del pilar, usa NUTRICION.\n");
        sb.append("- No recomendar suplementos, medicamentos ni tratamientos clínicos.\n");
        sb.append("- Formato literal en sustituciones: \"En lugar de X, puedes usar Y\".\n\n");

        // Bloque 8 - Instrucción final
        sb.append("Genera el objeto JSON con los 8 nodos según la estructura y restricciones ")
                .append("definidas. Recuerda:\n");
        sb.append("- Devuelve SOLO el JSON, sin texto adicional, sin markdown.\n");
        sb.append("- Si algún nodo nullable no aplica, devuélvelo como null literal.\n");
        sb.append("- Respeta exactamente los nombres de nodos y campos del schema.\n");

        return sb.toString();
    }

    private String nvl(Object v) {
        return v == null ? "No medido" : v.toString();
    }

    private String traducirSexo(String s) {
        if (s == null) return "No especificado";
        return switch (s) {
            case "M" -> "Masculino";
            case "F" -> "Femenino";
            default -> s;
        };
    }
}